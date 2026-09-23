package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.*;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import com.facturadorsri.backend.dto.comprobante.NotaCreditoRequest;
import com.facturadorsri.backend.dto.comprobante.VentaLineaRequest;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.exception.ResourceNotFoundException;
import com.facturadorsri.backend.repository.BodegaRepository;
import com.facturadorsri.backend.repository.ComprobanteRepository;
import com.facturadorsri.backend.repository.ProductoRepository;
import com.facturadorsri.backend.service.sri.AccessKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotaCreditoServiceTest {

    @Mock private ComprobanteRepository comprobanteRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private CatalogoSriService catalogoSriService;
    @Mock private SecuencialService secuencialService;
    @Mock private AccessKeyService accessKeyService;
    @Mock private InventarioService inventarioService;
    @Mock private ComprobanteProcesamientoService comprobanteProcesamientoService;
    @Mock private PlatformTransactionManager transactionManager;

    private NotaCreditoService service;

    private Comprobante original;
    private Bodega bodega;
    private Producto producto;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        service = new NotaCreditoService(comprobanteRepository, productoRepository, bodegaRepository,
                catalogoSriService, secuencialService, accessKeyService, inventarioService,
                comprobanteProcesamientoService, transactionManager);

        Emisor emisor = Emisor.builder().ruc("1790012344001").razonSocial("DEMO").direccionMatriz("x").build();
        Establecimiento establecimiento = Establecimiento.builder().id(1L).emisor(emisor).codigo("001").build();
        PuntoEmision puntoEmision = PuntoEmision.builder().id(1L).establecimiento(establecimiento).codigo("001").build();
        Cliente cliente = Cliente.builder().id(1L).tipoIdentificacion("05").identificacion("1725489635")
                .razonSocial("CLIENTE DEMO").build();
        bodega = Bodega.builder().id(1L).nombre("Principal").build();
        producto = Producto.builder().id(1L).codigoPrincipal("PROD-001").descripcion("Producto demo")
                .precioUnitario(new BigDecimal("10.00")).codigoImpuesto("2").codigoPorcentajeIva("4").build();

        original = Comprobante.builder().id(10L).tipoComprobante("01").estado(EstadoComprobante.AUTORIZADO)
                .establecimiento(establecimiento).puntoEmision(puntoEmision).secuencial("000000001")
                .fechaEmision(LocalDate.of(2026, 9, 22)).cliente(cliente).build();
    }

    @Test
    void rechazaLaNotaDeCreditoSiElComprobanteOriginalNoEstaAutorizado() {
        original.setEstado(EstadoComprobante.FIRMADO);
        when(comprobanteRepository.findById(10L)).thenReturn(Optional.of(original));

        NotaCreditoRequest request = new NotaCreditoRequest(10L, 1L, "Devolucion",
                List.of(new VentaLineaRequest(1L, BigDecimal.ONE, null)));

        assertThatThrownBy(() -> service.crear(request)).isInstanceOf(BusinessException.class);
        verify(comprobanteProcesamientoService, never()).procesar(any());
    }

    @Test
    void lanzaNoEncontradoSiElComprobanteOriginalNoExiste() {
        when(comprobanteRepository.findById(99L)).thenReturn(Optional.empty());

        NotaCreditoRequest request = new NotaCreditoRequest(99L, 1L, "Devolucion",
                List.of(new VentaLineaRequest(1L, BigDecimal.ONE, null)));

        assertThatThrownBy(() -> service.crear(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creaLaNotaDeCreditoYRestituyeElInventario() {
        when(comprobanteRepository.findById(10L)).thenReturn(Optional.of(original));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(catalogoSriService.validarVigente(any(), any()))
                .thenReturn(CatalogoSri.builder().valorNumerico(new BigDecimal("15.00")).build());
        when(secuencialService.siguienteNumero(1L, "04")).thenReturn("000000001");
        when(accessKeyService.generar(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("2209202601179001234400110010040000000011234567817");
        when(comprobanteRepository.save(any())).thenAnswer(inv -> {
            Comprobante c = inv.getArgument(0);
            c.setId(20L);
            return c;
        });
        when(comprobanteRepository.findById(20L)).thenReturn(Optional.of(
                Comprobante.builder().id(20L).cliente(original.getCliente())
                        .totalSinImpuestos(BigDecimal.ZERO).totalDescuento(BigDecimal.ZERO)
                        .importeTotal(BigDecimal.ZERO).estado(EstadoComprobante.GENERADO).build()));

        NotaCreditoRequest request = new NotaCreditoRequest(10L, 1L, "Producto defectuoso",
                List.of(new VentaLineaRequest(1L, BigDecimal.ONE, null)));

        service.crear(request);

        verify(inventarioService).registrar(eq(producto), eq(bodega),
                eq(com.facturadorsri.backend.domain.enums.TipoMovimientoInventario.DEVOLUCION_CLIENTE),
                eq(BigDecimal.ONE), isNull(), any(), any());
        verify(comprobanteProcesamientoService).procesar(20L);
    }
}
