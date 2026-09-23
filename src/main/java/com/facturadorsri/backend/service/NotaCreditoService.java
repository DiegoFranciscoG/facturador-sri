package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.*;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import com.facturadorsri.backend.domain.enums.NombreCatalogo;
import com.facturadorsri.backend.domain.enums.TipoComprobante;
import com.facturadorsri.backend.domain.enums.TipoMovimientoInventario;
import com.facturadorsri.backend.dto.comprobante.ComprobanteResponse;
import com.facturadorsri.backend.dto.comprobante.NotaCreditoRequest;
import com.facturadorsri.backend.dto.comprobante.VentaLineaRequest;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.exception.ResourceNotFoundException;
import com.facturadorsri.backend.repository.BodegaRepository;
import com.facturadorsri.backend.repository.ComprobanteRepository;
import com.facturadorsri.backend.repository.ProductoRepository;
import com.facturadorsri.backend.service.sri.AccessKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Nota de credito: siempre referencia el comprobante que modifica y exige
 * un motivo (docs/modelo-datos.md seccion 2). Restituye al inventario las
 * cantidades acreditadas (se asume devolucion fisica de mercaderia; un
 * ajuste puramente monetario sin devolucion queda fuera del MVP).
 */
@Service
@RequiredArgsConstructor
public class NotaCreditoService {

    private final ComprobanteRepository comprobanteRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final CatalogoSriService catalogoSriService;
    private final SecuencialService secuencialService;
    private final AccessKeyService accessKeyService;
    private final InventarioService inventarioService;
    private final ComprobanteProcesamientoService comprobanteProcesamientoService;

    @Transactional
    public ComprobanteResponse crear(NotaCreditoRequest request) {
        Comprobante original = comprobanteRepository.findById(request.comprobanteOriginalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comprobante original no encontrado: " + request.comprobanteOriginalId()));
        if (original.getEstado() != EstadoComprobante.AUTORIZADO) {
            throw new BusinessException("Solo se puede emitir una nota de credito sobre un comprobante AUTORIZADO");
        }
        Bodega bodega = bodegaRepository.findById(request.bodegaId())
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada: " + request.bodegaId()));

        LocalDate hoy = LocalDate.now();
        String secuencial = secuencialService.siguienteNumero(
                original.getPuntoEmision().getId(), TipoComprobante.NOTA_CREDITO.getCodigo());
        String ambiente = original.getEstablecimiento().getEmisor().getAmbiente();
        String claveAcceso = accessKeyService.generar(hoy, TipoComprobante.NOTA_CREDITO.getCodigo(),
                original.getEstablecimiento().getEmisor().getRuc(), ambiente,
                original.getEstablecimiento().getCodigo(), original.getPuntoEmision().getCodigo(), secuencial);

        Comprobante notaCredito = Comprobante.builder()
                .claveAcceso(claveAcceso)
                .tipoComprobante(TipoComprobante.NOTA_CREDITO.getCodigo())
                .establecimiento(original.getEstablecimiento())
                .puntoEmision(original.getPuntoEmision())
                .secuencial(secuencial)
                .fechaEmision(hoy)
                .ambiente(ambiente)
                .cliente(original.getCliente())
                .comprobanteRelacionado(original)
                .motivo(request.motivo())
                .build();

        BigDecimal totalSinImpuestos = BigDecimal.ZERO;
        BigDecimal totalImpuestos = BigDecimal.ZERO;

        for (VentaLineaRequest linea : request.lineas()) {
            Producto producto = productoRepository.findById(linea.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + linea.productoId()));

            BigDecimal subtotalLinea = producto.getPrecioUnitario()
                    .multiply(linea.cantidad())
                    .setScale(2, RoundingMode.HALF_UP);

            DetalleComprobante detalle = DetalleComprobante.builder()
                    .producto(producto)
                    .codigoPrincipal(producto.getCodigoPrincipal())
                    .descripcion(producto.getDescripcion())
                    .cantidad(linea.cantidad())
                    .precioUnitario(producto.getPrecioUnitario())
                    .descuento(BigDecimal.ZERO)
                    .precioTotalSinImpuesto(subtotalLinea)
                    .build();

            CatalogoSri tarifa = catalogoSriService.validarVigente(
                    NombreCatalogo.CODIGO_PORCENTAJE_IVA, producto.getCodigoPorcentajeIva());
            BigDecimal porcentaje = tarifa.getValorNumerico() != null ? tarifa.getValorNumerico() : BigDecimal.ZERO;
            BigDecimal valorImpuesto = subtotalLinea.multiply(porcentaje)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            detalle.agregarImpuesto(ImpuestoDetalle.builder()
                    .codigoImpuesto(producto.getCodigoImpuesto())
                    .codigoPorcentaje(producto.getCodigoPorcentajeIva())
                    .tarifa(porcentaje)
                    .baseImponible(subtotalLinea)
                    .valor(valorImpuesto)
                    .build());

            notaCredito.agregarDetalle(detalle);

            totalSinImpuestos = totalSinImpuestos.add(subtotalLinea);
            totalImpuestos = totalImpuestos.add(valorImpuesto);

            inventarioService.registrar(producto, bodega, TipoMovimientoInventario.DEVOLUCION_CLIENTE,
                    linea.cantidad(), null, notaCredito, "Nota de credito " + claveAcceso);
        }

        notaCredito.setTotalSinImpuestos(totalSinImpuestos);
        notaCredito.setTotalDescuento(BigDecimal.ZERO);
        notaCredito.setImporteTotal(totalSinImpuestos.add(totalImpuestos));

        notaCredito = comprobanteRepository.save(notaCredito);
        comprobanteProcesamientoService.procesar(notaCredito.getId());
        notaCredito = comprobanteRepository.findById(notaCredito.getId()).orElseThrow();

        return aResponse(notaCredito);
    }

    private ComprobanteResponse aResponse(Comprobante c) {
        return new ComprobanteResponse(c.getId(), c.getClaveAcceso(), c.getTipoComprobante(), c.getSecuencial(),
                c.getFechaEmision(), c.getEstado(), c.getTotalSinImpuestos(), c.getTotalDescuento(),
                c.getImporteTotal(), c.getNumeroAutorizacion(), c.getFechaAutorizacion(),
                c.getCliente().getRazonSocial());
    }
}
