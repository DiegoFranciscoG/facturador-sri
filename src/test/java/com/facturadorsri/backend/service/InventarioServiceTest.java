package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.Bodega;
import com.facturadorsri.backend.domain.MovimientoInventario;
import com.facturadorsri.backend.domain.Producto;
import com.facturadorsri.backend.domain.enums.TipoMovimientoInventario;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.repository.BodegaRepository;
import com.facturadorsri.backend.repository.MovimientoInventarioRepository;
import com.facturadorsri.backend.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private MovimientoInventarioRepository movimientoRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private BodegaRepository bodegaRepository;

    private InventarioService service;

    private Producto producto;
    private Bodega bodega;

    @BeforeEach
    void setUp() {
        service = new InventarioService(movimientoRepository, productoRepository, bodegaRepository);
        producto = Producto.builder().id(1L).descripcion("Producto demo").build();
        bodega = Bodega.builder().id(1L).nombre("Bodega principal").build();
    }

    @Test
    void unaEntradaSumaAlSaldoAnterior() {
        when(movimientoRepository.findFirstByProductoIdAndBodegaIdOrderByIdDesc(1L, 1L))
                .thenReturn(Optional.of(MovimientoInventario.builder().saldoCantidad(new BigDecimal("10")).build()));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MovimientoInventario resultado = service.registrar(producto, bodega,
                TipoMovimientoInventario.ENTRADA_COMPRA, new BigDecimal("5"), new BigDecimal("2.00"), null, null);

        assertThat(resultado.getSaldoCantidad()).isEqualByComparingTo("15");
    }

    @Test
    void unaSalidaRestaDelSaldoAnterior() {
        when(movimientoRepository.findFirstByProductoIdAndBodegaIdOrderByIdDesc(1L, 1L))
                .thenReturn(Optional.of(MovimientoInventario.builder().saldoCantidad(new BigDecimal("10")).build()));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MovimientoInventario resultado = service.registrar(producto, bodega,
                TipoMovimientoInventario.SALIDA_VENTA, new BigDecimal("4"), null, null, null);

        assertThat(resultado.getSaldoCantidad()).isEqualByComparingTo("6");
    }

    @Test
    void rechazaUnaSalidaQueDejariaElSaldoNegativo() {
        when(movimientoRepository.findFirstByProductoIdAndBodegaIdOrderByIdDesc(1L, 1L))
                .thenReturn(Optional.of(MovimientoInventario.builder().saldoCantidad(new BigDecimal("3")).build()));

        assertThatThrownBy(() -> service.registrar(producto, bodega, TipoMovimientoInventario.SALIDA_VENTA,
                new BigDecimal("5"), null, null, null))
                .isInstanceOf(BusinessException.class);
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void elPrimerMovimientoPartesDeSaldoCero() {
        when(movimientoRepository.findFirstByProductoIdAndBodegaIdOrderByIdDesc(1L, 1L))
                .thenReturn(Optional.empty());
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MovimientoInventario resultado = service.registrar(producto, bodega,
                TipoMovimientoInventario.ENTRADA_COMPRA, new BigDecimal("20"), null, null, null);

        assertThat(resultado.getSaldoCantidad()).isEqualByComparingTo("20");
    }
}
