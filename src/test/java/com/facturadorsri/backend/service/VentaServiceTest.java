package com.facturadorsri.backend.service;

import com.facturadorsri.backend.config.SriProperties;
import com.facturadorsri.backend.domain.*;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import com.facturadorsri.backend.dto.comprobante.PagoRequest;
import com.facturadorsri.backend.dto.comprobante.VentaLineaRequest;
import com.facturadorsri.backend.dto.comprobante.VentaRequest;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.repository.*;
import com.facturadorsri.backend.service.sri.AccessKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private EstablecimientoRepository establecimientoRepository;
    @Mock private PuntoEmisionRepository puntoEmisionRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private ComprobanteRepository comprobanteRepository;
    @Mock private CatalogoSriService catalogoSriService;
    @Mock private SecuencialService secuencialService;
    @Mock private AccessKeyService accessKeyService;
    @Mock private InventarioService inventarioService;
    @Mock private ComprobanteProcesamientoService comprobanteProcesamientoService;
    @Mock private PlatformTransactionManager transactionManager;

    private VentaService service;

    private Establecimiento establecimiento;
    private PuntoEmision puntoEmision;
    private Cliente cliente;
    private Bodega bodega;
    private Producto producto;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        service = new VentaService(establecimientoRepository, puntoEmisionRepository, clienteRepository,
                bodegaRepository, productoRepository, comprobanteRepository, catalogoSriService,
                secuencialService, accessKeyService, inventarioService,
                new SriProperties("SIMULADO", "1", "x", "y", null, null, "2.34",
                        new SriProperties.Reintentos(5, 30)),
                comprobanteProcesamientoService, transactionManager);

        Emisor emisor = Emisor.builder().ruc("1790012344001").razonSocial("DEMO")
                .direccionMatriz("x").ambiente("1").build();
        establecimiento = Establecimiento.builder().id(1L).emisor(emisor).codigo("001").build();
        puntoEmision = PuntoEmision.builder().id(1L).establecimiento(establecimiento).codigo("001").build();
        cliente = Cliente.builder().id(1L).tipoIdentificacion("05").identificacion("1725489635")
                .razonSocial("CLIENTE DEMO").build();
        bodega = Bodega.builder().id(1L).nombre("Principal").build();
        producto = Producto.builder().id(1L).codigoPrincipal("PROD-001").descripcion("Producto demo")
                .precioUnitario(new BigDecimal("10.00")).codigoImpuesto("2").codigoPorcentajeIva("4")
                .activo(true).build();

        lenient().when(establecimientoRepository.findById(1L)).thenReturn(Optional.of(establecimiento));
        lenient().when(puntoEmisionRepository.findById(1L)).thenReturn(Optional.of(puntoEmision));
        lenient().when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        lenient().when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        lenient().when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        lenient().when(catalogoSriService.validarVigente(any(), any())).thenReturn(
                CatalogoSri.builder().valorNumerico(new BigDecimal("15.00")).build());
        lenient().when(secuencialService.siguienteNumero(1L, "01")).thenReturn("000000001");
        lenient().when(accessKeyService.generar(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("2209202601179001234400110010010000000011234567816");
        lenient().when(comprobanteRepository.save(any())).thenAnswer(inv -> {
            Comprobante c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(1L);
            }
            return c;
        });
        lenient().when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(
                Comprobante.builder().id(1L).cliente(cliente).estado(EstadoComprobante.GENERADO)
                        .totalSinImpuestos(BigDecimal.ZERO).totalDescuento(BigDecimal.ZERO)
                        .importeTotal(BigDecimal.ZERO).build()));
    }

    @Test
    void rechazaLaVentaSiLaSumaDePagosNoCoincideConElTotal() {
        VentaRequest request = new VentaRequest(1L, 1L, 1L, 1L,
                java.util.List.of(new VentaLineaRequest(1L, BigDecimal.ONE, null)),
                java.util.List.of(new PagoRequest("01", new BigDecimal("999.00"), null, null)));

        assertThatThrownBy(() -> service.crearFactura(request)).isInstanceOf(BusinessException.class);
        verify(comprobanteProcesamientoService, never()).procesar(any());
    }

    @Test
    void rechazaLaVentaSiElProductoEstaInactivo() {
        producto.setActivo(false);
        VentaRequest request = new VentaRequest(1L, 1L, 1L, 1L,
                java.util.List.of(new VentaLineaRequest(1L, BigDecimal.ONE, null)),
                java.util.List.of(new PagoRequest("01", new BigDecimal("11.50"), null, null)));

        assertThatThrownBy(() -> service.crearFactura(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void calculaElIvaCorrectamenteYDisparaElProcesamiento() {
        VentaRequest request = new VentaRequest(1L, 1L, 1L, 1L,
                java.util.List.of(new VentaLineaRequest(1L, BigDecimal.ONE, null)),
                java.util.List.of(new PagoRequest("01", new BigDecimal("11.50"), null, null)));

        service.crearFactura(request);

        verify(inventarioService).registrar(eq(producto), eq(bodega), any(), eq(BigDecimal.ONE), isNull(), any(), any());
        verify(comprobanteProcesamientoService).procesar(1L);
    }
}
