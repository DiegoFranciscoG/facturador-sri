package com.facturadorsri.backend.service.ride;

import com.facturadorsri.backend.domain.*;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import com.facturadorsri.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RidePdfServiceTest {

    private final RidePdfService service = new RidePdfService();

    @Test
    void generaUnPdfValidoParaUnComprobanteAutorizado() {
        byte[] pdf = service.generar(comprobanteAutorizadoDeEjemplo());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
    }

    @Test
    void rechazaGenerarRideParaUnComprobanteNoAutorizado() {
        Comprobante comprobante = comprobanteAutorizadoDeEjemplo();
        comprobante.setEstado(EstadoComprobante.FIRMADO);

        assertThatThrownBy(() -> service.generar(comprobante)).isInstanceOf(BusinessException.class);
    }

    private Comprobante comprobanteAutorizadoDeEjemplo() {
        Emisor emisor = Emisor.builder()
                .ruc("1790012344001").razonSocial("COMERCIAL DEMO CIA. LTDA.")
                .direccionMatriz("Av. Ficticia N34-56, Quito").obligadoContabilidad(true).build();
        Establecimiento establecimiento = Establecimiento.builder()
                .emisor(emisor).codigo("001").direccion("Av. Ficticia N34-56, Quito").activo(true).build();
        PuntoEmision puntoEmision = PuntoEmision.builder()
                .establecimiento(establecimiento).codigo("001").descripcion("Caja").activo(true).build();
        Cliente cliente = Cliente.builder()
                .tipoIdentificacion("05").identificacion("1725489635")
                .razonSocial("CLIENTE DEMO").direccion("Calle Ficticia 123").build();
        Producto producto = Producto.builder()
                .codigoPrincipal("PROD-001").descripcion("Producto demo")
                .unidadMedida("UNIDAD").precioUnitario(new BigDecimal("10.00")).build();

        Comprobante comprobante = Comprobante.builder()
                .claveAcceso("2209202601179001234400110010010000000011234567816")
                .tipoComprobante("01")
                .establecimiento(establecimiento)
                .puntoEmision(puntoEmision)
                .secuencial("000000001")
                .fechaEmision(LocalDate.of(2026, 9, 22))
                .ambiente("1")
                .cliente(cliente)
                .estado(EstadoComprobante.AUTORIZADO)
                .numeroAutorizacion("2209202601179001234400110010010000000011234567816")
                .fechaAutorizacion(LocalDateTime.now())
                .totalSinImpuestos(new BigDecimal("10.00"))
                .totalDescuento(BigDecimal.ZERO)
                .importeTotal(new BigDecimal("11.50"))
                .build();

        DetalleComprobante detalle = DetalleComprobante.builder()
                .producto(producto).codigoPrincipal(producto.getCodigoPrincipal())
                .descripcion(producto.getDescripcion()).cantidad(BigDecimal.ONE)
                .precioUnitario(producto.getPrecioUnitario()).descuento(BigDecimal.ZERO)
                .precioTotalSinImpuesto(new BigDecimal("10.00")).build();
        comprobante.agregarDetalle(detalle);
        comprobante.agregarPago(Pago.builder().formaPago("01").total(new BigDecimal("11.50")).build());

        return comprobante;
    }
}
