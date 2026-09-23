package com.facturadorsri.backend.service;

import com.facturadorsri.backend.config.SriProperties;
import com.facturadorsri.backend.domain.*;
import com.facturadorsri.backend.domain.enums.NombreCatalogo;
import com.facturadorsri.backend.domain.enums.TipoComprobante;
import com.facturadorsri.backend.domain.enums.TipoMovimientoInventario;
import com.facturadorsri.backend.dto.comprobante.ComprobanteResponse;
import com.facturadorsri.backend.dto.comprobante.PagoRequest;
import com.facturadorsri.backend.dto.comprobante.VentaLineaRequest;
import com.facturadorsri.backend.dto.comprobante.VentaRequest;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.exception.ResourceNotFoundException;
import com.facturadorsri.backend.repository.*;
import com.facturadorsri.backend.service.sri.AccessKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Orquesta la venta POS -> factura: calcula totales e impuestos, asigna el
 * secuencial (bloqueo pesimista), genera la clave de acceso, registra el
 * kardex de salida y deja el comprobante listo para que
 * ComprobanteProcesamientoService lo firme y envie al SRI.
 *
 * La creacion se hace en su propia transaccion (TransactionTemplate) que
 * hace commit ANTES de invocar a ComprobanteProcesamientoService: ese
 * servicio abre una transaccion NUEVA (REQUIRES_NEW) para no retener el
 * lock del secuencial durante la llamada SOAP, y una transaccion nueva
 * solo ve datos ya comprometidos, no los de una transaccion todavia abierta
 * en el mismo hilo.
 */
@Service
public class VentaService {

    private final EstablecimientoRepository establecimientoRepository;
    private final PuntoEmisionRepository puntoEmisionRepository;
    private final ClienteRepository clienteRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final CatalogoSriService catalogoSriService;
    private final SecuencialService secuencialService;
    private final AccessKeyService accessKeyService;
    private final InventarioService inventarioService;
    private final SriProperties sriProperties;
    private final ComprobanteProcesamientoService comprobanteProcesamientoService;
    private final TransactionTemplate transactionTemplate;

    public VentaService(EstablecimientoRepository establecimientoRepository,
                         PuntoEmisionRepository puntoEmisionRepository,
                         ClienteRepository clienteRepository,
                         BodegaRepository bodegaRepository,
                         ProductoRepository productoRepository,
                         ComprobanteRepository comprobanteRepository,
                         CatalogoSriService catalogoSriService,
                         SecuencialService secuencialService,
                         AccessKeyService accessKeyService,
                         InventarioService inventarioService,
                         SriProperties sriProperties,
                         ComprobanteProcesamientoService comprobanteProcesamientoService,
                         PlatformTransactionManager transactionManager) {
        this.establecimientoRepository = establecimientoRepository;
        this.puntoEmisionRepository = puntoEmisionRepository;
        this.clienteRepository = clienteRepository;
        this.bodegaRepository = bodegaRepository;
        this.productoRepository = productoRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.catalogoSriService = catalogoSriService;
        this.secuencialService = secuencialService;
        this.accessKeyService = accessKeyService;
        this.inventarioService = inventarioService;
        this.sriProperties = sriProperties;
        this.comprobanteProcesamientoService = comprobanteProcesamientoService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public ComprobanteResponse crearFactura(VentaRequest request) {
        Long comprobanteId = transactionTemplate.execute(status -> crearComprobantePendiente(request));
        comprobanteProcesamientoService.procesar(comprobanteId);
        // Transaccion propia: procesar() ya cerro la suya, y sin sesion
        // activa la asociacion perezosa cliente no se puede leer.
        return transactionTemplate.execute(status ->
                aResponse(comprobanteRepository.findById(comprobanteId).orElseThrow()));
    }

    private Long crearComprobantePendiente(VentaRequest request) {
        Establecimiento establecimiento = establecimientoRepository.findById(request.establecimientoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Establecimiento no encontrado: " + request.establecimientoId()));
        PuntoEmision puntoEmision = puntoEmisionRepository.findById(request.puntoEmisionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Punto de emision no encontrado: " + request.puntoEmisionId()));
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.clienteId()));
        Bodega bodega = bodegaRepository.findById(request.bodegaId())
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada: " + request.bodegaId()));

        request.pagos().forEach(p -> catalogoSriService.validarVigente(NombreCatalogo.FORMA_PAGO, p.formaPago()));

        LocalDate hoy = LocalDate.now();
        String secuencial = secuencialService.siguienteNumero(
                puntoEmision.getId(), TipoComprobante.FACTURA.getCodigo());
        String ambiente = establecimiento.getEmisor().getAmbiente();
        String claveAcceso = accessKeyService.generar(hoy, TipoComprobante.FACTURA.getCodigo(),
                establecimiento.getEmisor().getRuc(), ambiente, establecimiento.getCodigo(),
                puntoEmision.getCodigo(), secuencial);

        Comprobante comprobante = Comprobante.builder()
                .claveAcceso(claveAcceso)
                .tipoComprobante(TipoComprobante.FACTURA.getCodigo())
                .establecimiento(establecimiento)
                .puntoEmision(puntoEmision)
                .secuencial(secuencial)
                .fechaEmision(hoy)
                .ambiente(ambiente)
                .cliente(cliente)
                .build();

        BigDecimal totalSinImpuestos = BigDecimal.ZERO;
        BigDecimal totalDescuento = BigDecimal.ZERO;
        BigDecimal totalImpuestos = BigDecimal.ZERO;

        for (VentaLineaRequest linea : request.lineas()) {
            Producto producto = productoRepository.findById(linea.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + linea.productoId()));
            if (!producto.isActivo()) {
                throw new BusinessException("El producto '%s' esta inactivo".formatted(producto.getDescripcion()));
            }

            BigDecimal descuento = linea.descuento() != null ? linea.descuento() : BigDecimal.ZERO;
            BigDecimal subtotalLinea = producto.getPrecioUnitario()
                    .multiply(linea.cantidad())
                    .subtract(descuento)
                    .setScale(2, RoundingMode.HALF_UP);

            DetalleComprobante detalle = DetalleComprobante.builder()
                    .producto(producto)
                    .codigoPrincipal(producto.getCodigoPrincipal())
                    .descripcion(producto.getDescripcion())
                    .cantidad(linea.cantidad())
                    .precioUnitario(producto.getPrecioUnitario())
                    .descuento(descuento)
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

            comprobante.agregarDetalle(detalle);

            totalSinImpuestos = totalSinImpuestos.add(subtotalLinea);
            totalDescuento = totalDescuento.add(descuento);
            totalImpuestos = totalImpuestos.add(valorImpuesto);

            inventarioService.registrar(producto, bodega, TipoMovimientoInventario.SALIDA_VENTA,
                    linea.cantidad(), null, comprobante, "Venta " + claveAcceso);
        }

        comprobante.setTotalSinImpuestos(totalSinImpuestos);
        comprobante.setTotalDescuento(totalDescuento);
        comprobante.setImporteTotal(totalSinImpuestos.add(totalImpuestos));

        BigDecimal totalPagos = BigDecimal.ZERO;
        for (PagoRequest pago : request.pagos()) {
            comprobante.agregarPago(Pago.builder()
                    .formaPago(pago.formaPago())
                    .total(pago.total())
                    .plazo(pago.plazo())
                    .unidadTiempo(pago.unidadTiempo())
                    .build());
            totalPagos = totalPagos.add(pago.total());
        }
        if (totalPagos.compareTo(comprobante.getImporteTotal()) != 0) {
            throw new BusinessException("La suma de los pagos (%s) no coincide con el total de la factura (%s)"
                    .formatted(totalPagos, comprobante.getImporteTotal()));
        }

        comprobante = comprobanteRepository.save(comprobante);
        return comprobante.getId();
    }

    private ComprobanteResponse aResponse(Comprobante c) {
        return new ComprobanteResponse(c.getId(), c.getClaveAcceso(), c.getTipoComprobante(), c.getSecuencial(),
                c.getFechaEmision(), c.getEstado(), c.getTotalSinImpuestos(), c.getTotalDescuento(),
                c.getImporteTotal(), c.getNumeroAutorizacion(), c.getFechaAutorizacion(),
                c.getCliente().getRazonSocial());
    }
}
