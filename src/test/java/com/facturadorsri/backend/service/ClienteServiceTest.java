package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.Cliente;
import com.facturadorsri.backend.domain.CatalogoSri;
import com.facturadorsri.backend.dto.cliente.ClienteRequest;
import com.facturadorsri.backend.dto.cliente.ClienteResponse;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.mapper.ClienteMapper;
import com.facturadorsri.backend.repository.ClienteRepository;
import com.facturadorsri.backend.service.sri.CedulaValidator;
import com.facturadorsri.backend.service.sri.RucValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ClienteMapper clienteMapper;
    @Mock
    private CatalogoSriService catalogoSriService;

    private ClienteService service;

    @BeforeEach
    void setUp() {
        service = new ClienteService(clienteRepository, clienteMapper, catalogoSriService,
                new RucValidator(new CedulaValidator()), new CedulaValidator());
    }

    @Test
    void creaUnClientePersonaNaturalConCedulaValida() {
        ClienteRequest request = new ClienteRequest("05", "1725489635", "CLIENTE DEMO", null, null, null);
        Cliente entidad = new Cliente();
        when(clienteRepository.findByTipoIdentificacionAndIdentificacion("05", "1725489635"))
                .thenReturn(Optional.empty());
        when(clienteMapper.aEntidad(request)).thenReturn(entidad);
        when(clienteRepository.save(entidad)).thenReturn(entidad);
        when(clienteMapper.aResponse(entidad)).thenReturn(
                new ClienteResponse(1L, "05", "1725489635", "CLIENTE DEMO", null, null, null));
        when(catalogoSriService.validarVigente(any(), eq("05"))).thenReturn(new CatalogoSri());

        ClienteResponse response = service.crear(request);

        assertThat(response.identificacion()).isEqualTo("1725489635");
        verify(clienteRepository).save(entidad);
    }

    @Test
    void rechazaUnaCedulaConDigitoVerificadorInvalido() {
        ClienteRequest request = new ClienteRequest("05", "1725489634", "CLIENTE DEMO", null, null, null);
        when(catalogoSriService.validarVigente(any(), eq("05"))).thenReturn(new CatalogoSri());

        assertThatThrownBy(() -> service.crear(request)).isInstanceOf(BusinessException.class);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void rechazaUnRucInvalidoParaTipoIdentificacionRuc() {
        ClienteRequest request = new ClienteRequest("04", "1790012345001", "EMPRESA DEMO", null, null, null);
        when(catalogoSriService.validarVigente(any(), eq("04"))).thenReturn(new CatalogoSri());

        assertThatThrownBy(() -> service.crear(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void aceptaConsumidorFinalConLos13Nueves() {
        ClienteRequest request = new ClienteRequest("07", "9999999999999", "CONSUMIDOR FINAL", null, null, null);
        Cliente entidad = new Cliente();
        when(clienteRepository.findByTipoIdentificacionAndIdentificacion("07", "9999999999999"))
                .thenReturn(Optional.empty());
        when(clienteMapper.aEntidad(request)).thenReturn(entidad);
        when(clienteRepository.save(entidad)).thenReturn(entidad);
        when(clienteMapper.aResponse(entidad)).thenReturn(
                new ClienteResponse(1L, "07", "9999999999999", "CONSUMIDOR FINAL", null, null, null));
        when(catalogoSriService.validarVigente(any(), eq("07"))).thenReturn(new CatalogoSri());

        assertThat(service.crear(request).identificacion()).isEqualTo("9999999999999");
    }

    @Test
    void rechazaClienteDuplicadoPorTipoEIdentificacion() {
        ClienteRequest request = new ClienteRequest("05", "1725489635", "CLIENTE DEMO", null, null, null);
        when(catalogoSriService.validarVigente(any(), eq("05"))).thenReturn(new CatalogoSri());
        when(clienteRepository.findByTipoIdentificacionAndIdentificacion("05", "1725489635"))
                .thenReturn(Optional.of(new Cliente()));

        assertThatThrownBy(() -> service.crear(request)).isInstanceOf(BusinessException.class);
        verify(clienteRepository, never()).save(any());
    }
}
