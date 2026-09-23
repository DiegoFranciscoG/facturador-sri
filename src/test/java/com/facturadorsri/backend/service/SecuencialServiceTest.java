package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.Establecimiento;
import com.facturadorsri.backend.domain.PuntoEmision;
import com.facturadorsri.backend.domain.Secuencial;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.exception.ResourceNotFoundException;
import com.facturadorsri.backend.repository.PuntoEmisionRepository;
import com.facturadorsri.backend.repository.SecuencialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecuencialServiceTest {

    @Mock
    private SecuencialRepository secuencialRepository;
    @Mock
    private PuntoEmisionRepository puntoEmisionRepository;

    private SecuencialService service;

    @BeforeEach
    void setUp() {
        service = new SecuencialService(secuencialRepository, puntoEmisionRepository);
    }

    @Test
    void devuelveElSiguienteNumeroConCerosALaIzquierda() {
        Secuencial secuencial = Secuencial.builder().puntoEmision(new PuntoEmision()).tipoComprobante("01")
                .ultimoNumero(41).build();
        when(secuencialRepository.buscarParaActualizar(1L, "01")).thenReturn(Optional.of(secuencial));
        when(secuencialRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String siguiente = service.siguienteNumero(1L, "01");

        assertThat(siguiente).isEqualTo("000000042");
        assertThat(secuencial.getUltimoNumero()).isEqualTo(42);
    }

    @Test
    void creaElSecuencialEnCeroSiNoExisteTodavia() {
        when(secuencialRepository.buscarParaActualizar(1L, "04")).thenReturn(Optional.empty());
        when(puntoEmisionRepository.findById(1L)).thenReturn(Optional.of(new PuntoEmision()));
        when(secuencialRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String siguiente = service.siguienteNumero(1L, "04");

        assertThat(siguiente).isEqualTo("000000001");
    }

    @Test
    void lanzaExcepcionSiElPuntoDeEmisionNoExistePararCrearElSecuencial() {
        when(secuencialRepository.buscarParaActualizar(99L, "01")).thenReturn(Optional.empty());
        when(puntoEmisionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.siguienteNumero(99L, "01"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void lanzaExcepcionCuandoElSecuencialSeAgota() {
        Secuencial secuencial = Secuencial.builder().puntoEmision(new PuntoEmision()).tipoComprobante("01")
                .ultimoNumero(999_999_999L).build();
        when(secuencialRepository.buscarParaActualizar(1L, "01")).thenReturn(Optional.of(secuencial));

        assertThatThrownBy(() -> service.siguienteNumero(1L, "01"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void guardaElSecuencialActualizadoUnaSolaVez() {
        Secuencial secuencial = Secuencial.builder().puntoEmision(new PuntoEmision()).tipoComprobante("01")
                .ultimoNumero(0).build();
        when(secuencialRepository.buscarParaActualizar(1L, "01")).thenReturn(Optional.of(secuencial));
        when(secuencialRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.siguienteNumero(1L, "01");

        ArgumentCaptor<Secuencial> captor = ArgumentCaptor.forClass(Secuencial.class);
        verify(secuencialRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getUltimoNumero()).isEqualTo(1);
    }
}
