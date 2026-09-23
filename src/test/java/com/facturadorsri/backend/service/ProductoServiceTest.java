package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.CatalogoSri;
import com.facturadorsri.backend.domain.Producto;
import com.facturadorsri.backend.dto.producto.ProductoRequest;
import com.facturadorsri.backend.dto.producto.ProductoResponse;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.mapper.ProductoMapper;
import com.facturadorsri.backend.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private ProductoMapper productoMapper;
    @Mock
    private CatalogoSriService catalogoSriService;

    private ProductoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoService(productoRepository, productoMapper, catalogoSriService);
    }

    private ProductoRequest requestValido() {
        return new ProductoRequest("PROD-001", "AUX-001", "Producto demo", "UNIDAD",
                new BigDecimal("10.00"), "2", "4");
    }

    @Test
    void creaUnProductoNuevoActivoPorDefecto() {
        ProductoRequest request = requestValido();
        Producto entidad = new Producto();
        when(catalogoSriService.validarVigente(any(), eq("2"))).thenReturn(new CatalogoSri());
        when(catalogoSriService.validarVigente(any(), eq("4"))).thenReturn(new CatalogoSri());
        when(productoRepository.existsByCodigoPrincipal("PROD-001")).thenReturn(false);
        when(productoMapper.aEntidad(request)).thenReturn(entidad);
        when(productoRepository.save(entidad)).thenReturn(entidad);
        when(productoMapper.aResponse(entidad)).thenReturn(
                new ProductoResponse(1L, "PROD-001", "AUX-001", "Producto demo", "UNIDAD",
                        new BigDecimal("10.00"), "2", "4", true));

        ProductoResponse response = service.crear(request);

        assertThat(response.activo()).isTrue();
        assertThat(entidad.isActivo()).isTrue();
    }

    @Test
    void rechazaCodigoPrincipalDuplicado() {
        ProductoRequest request = requestValido();
        when(catalogoSriService.validarVigente(any(), eq("2"))).thenReturn(new CatalogoSri());
        when(catalogoSriService.validarVigente(any(), eq("4"))).thenReturn(new CatalogoSri());
        when(productoRepository.existsByCodigoPrincipal("PROD-001")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request)).isInstanceOf(BusinessException.class);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void desactivarMarcaElProductoComoInactivo() {
        Producto producto = Producto.builder().id(1L).activo(true).build();
        when(productoRepository.findById(1L)).thenReturn(java.util.Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        service.desactivar(1L);

        assertThat(producto.isActivo()).isFalse();
    }
}
