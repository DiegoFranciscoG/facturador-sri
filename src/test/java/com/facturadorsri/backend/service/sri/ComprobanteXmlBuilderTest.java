package com.facturadorsri.backend.service.sri;

import com.facturadorsri.backend.domain.*;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ComprobanteXmlBuilderTest {

    private final ComprobanteXmlBuilder builder = new ComprobanteXmlBuilder();

    @Test
    void generaUnXmlDeFacturaBienFormadoConLosCamposClave() throws Exception {
        Comprobante factura = comprobanteFacturaDeEjemplo();

        String xml = builder.construirFacturaXml(factura);

        var documento = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
        XPath xpath = XPathFactory.newInstance().newXPath();

        assertThat(documento.getDocumentElement().getTagName()).isEqualTo("factura");
        assertThat(documento.getDocumentElement().getAttribute("id")).isEqualTo("comprobante");
        assertThat(xpath.evaluate("/factura/infoTributaria/claveAcceso", documento))
                .isEqualTo(factura.getClaveAcceso());
        assertThat(xpath.evaluate("/factura/infoTributaria/codDoc", documento)).isEqualTo("01");
        assertThat(xpath.evaluate("/factura/infoFactura/importeTotal", documento)).isEqualTo("11.50");
        assertThat(xpath.evaluate("/factura/infoFactura/totalConImpuestos/totalImpuesto/codigoPorcentaje", documento))
                .isEqualTo("4");
        assertThat(xpath.evaluate("count(/factura/detalles/detalle)", documento)).isEqualTo("1");
        assertThat(xpath.evaluate("/factura/detalles/detalle/cantidad", documento)).isEqualTo("2.000000");
    }

    @Test
    void rechazaGenerarFacturaSiElComprobanteNoEsFactura() {
        Comprobante notaCredito = comprobanteFacturaDeEjemplo();
        notaCredito.setTipoComprobante("04");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> builder.construirFacturaXml(notaCredito));
    }

    private Comprobante comprobanteFacturaDeEjemplo() {
        Emisor emisor = Emisor.builder()
                .ruc("1790012344001")
                .razonSocial("COMERCIAL DEMO CIA. LTDA.")
                .nombreComercial("DEMO STORE")
                .direccionMatriz("Av. Ficticia N34-56, Quito")
                .obligadoContabilidad(true)
                .build();
        Establecimiento establecimiento = Establecimiento.builder()
                .emisor(emisor).codigo("001").direccion("Av. Ficticia N34-56, Quito").activo(true).build();
        PuntoEmision puntoEmision = PuntoEmision.builder()
                .establecimiento(establecimiento).codigo("001").descripcion("Caja").activo(true).build();
        Cliente cliente = Cliente.builder()
                .tipoIdentificacion("05").identificacion("1725489635")
                .razonSocial("CLIENTE DEMO").direccion("Calle Ficticia 123").build();

        Producto producto = Producto.builder()
                .codigoPrincipal("PROD-001").descripcion("Producto demo con IVA 15%")
                .unidadMedida("UNIDAD").precioUnitario(new BigDecimal("5.00"))
                .codigoImpuesto("2").codigoPorcentajeIva("4").activo(true).build();

        Comprobante comprobante = Comprobante.builder()
                .claveAcceso("2209202601179001234400110010010000000011234567816")
                .tipoComprobante("01")
                .establecimiento(establecimiento)
                .puntoEmision(puntoEmision)
                .secuencial("000000001")
                .fechaEmision(LocalDate.of(2026, 9, 22))
                .ambiente("1")
                .tipoEmision("1")
                .cliente(cliente)
                .estado(EstadoComprobante.GENERADO)
                .totalSinImpuestos(new BigDecimal("10.00"))
                .totalDescuento(BigDecimal.ZERO)
                .importeTotal(new BigDecimal("11.50"))
                .build();

        DetalleComprobante detalle = DetalleComprobante.builder()
                .producto(producto)
                .codigoPrincipal(producto.getCodigoPrincipal())
                .descripcion(producto.getDescripcion())
                .cantidad(new BigDecimal("2"))
                .precioUnitario(producto.getPrecioUnitario())
                .descuento(BigDecimal.ZERO)
                .precioTotalSinImpuesto(new BigDecimal("10.00"))
                .build();
        detalle.agregarImpuesto(ImpuestoDetalle.builder()
                .codigoImpuesto("2").codigoPorcentaje("4")
                .tarifa(new BigDecimal("15.00")).baseImponible(new BigDecimal("10.00"))
                .valor(new BigDecimal("1.50")).build());
        comprobante.agregarDetalle(detalle);

        comprobante.agregarPago(Pago.builder().formaPago("01").total(new BigDecimal("11.50")).build());

        return comprobante;
    }
}
