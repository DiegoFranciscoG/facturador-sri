package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.Bodega;
import com.facturadorsri.backend.domain.Comprobante;
import com.facturadorsri.backend.domain.MovimientoInventario;
import com.facturadorsri.backend.domain.Producto;
import com.facturadorsri.backend.domain.enums.TipoMovimientoInventario;
import com.facturadorsri.backend.dto.inventario.MovimientoInventarioRequest;
import com.facturadorsri.backend.dto.inventario.MovimientoInventarioResponse;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.exception.ResourceNotFoundException;
import com.facturadorsri.backend.repository.BodegaRepository;
import com.facturadorsri.backend.repository.MovimientoInventarioRepository;
import com.facturadorsri.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Kardex: el saldo de cada producto por bodega se deriva del ultimo
 * movimiento (saldo_cantidad), nunca de una tabla de existencias aparte.
 * Ver docs/modelo-datos.md seccion 1 (MOVIMIENTO_INVENTARIO).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;

    public MovimientoInventarioResponse registrarMovimiento(MovimientoInventarioRequest request) {
        Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + request.productoId()));
        Bodega bodega = bodegaRepository.findById(request.bodegaId())
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada: " + request.bodegaId()));

        MovimientoInventario movimiento = registrar(producto, bodega, request.tipoMovimiento(),
                request.cantidad(), request.costoUnitario(), null, request.observacion());
        return aResponse(movimiento);
    }

    /** Usado por el flujo de venta/nota de credito (VentaService) dentro de su propia transaccion. */
    public MovimientoInventario registrar(Producto producto, Bodega bodega, TipoMovimientoInventario tipo,
                                           BigDecimal cantidad, BigDecimal costoUnitario,
                                           Comprobante comprobante, String observacion) {
        BigDecimal saldoAnterior = saldoActual(producto.getId(), bodega.getId());
        BigDecimal nuevoSaldo = tipo.esEntrada() ? saldoAnterior.add(cantidad) : saldoAnterior.subtract(cantidad);

        if (!tipo.esEntrada() && nuevoSaldo.signum() < 0) {
            throw new BusinessException("Stock insuficiente de '%s' en la bodega seleccionada (disponible: %s)"
                    .formatted(producto.getDescripcion(), saldoAnterior));
        }

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .bodega(bodega)
                .tipoMovimiento(tipo)
                .cantidad(cantidad)
                .costoUnitario(costoUnitario)
                .saldoCantidad(nuevoSaldo)
                .comprobante(comprobante)
                .observacion(observacion)
                .build();
        return movimientoRepository.save(movimiento);
    }

    @Transactional(readOnly = true)
    public BigDecimal saldoActual(Long productoId, Long bodegaId) {
        return movimientoRepository.findFirstByProductoIdAndBodegaIdOrderByIdDesc(productoId, bodegaId)
                .map(MovimientoInventario::getSaldoCantidad)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> kardex(Long productoId, Long bodegaId) {
        return movimientoRepository.findByProductoIdAndBodegaIdOrderByFechaDesc(productoId, bodegaId).stream()
                .map(this::aResponse)
                .toList();
    }

    private MovimientoInventarioResponse aResponse(MovimientoInventario m) {
        return new MovimientoInventarioResponse(
                m.getId(), m.getProducto().getId(), m.getBodega().getId(), m.getTipoMovimiento(),
                m.getCantidad(), m.getCostoUnitario(), m.getSaldoCantidad(),
                m.getComprobante() != null ? m.getComprobante().getId() : null,
                m.getFecha(), m.getObservacion());
    }
}
