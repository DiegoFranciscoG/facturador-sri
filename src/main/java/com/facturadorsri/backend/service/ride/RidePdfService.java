package com.facturadorsri.backend.service.ride;

import com.facturadorsri.backend.domain.Comprobante;
import com.facturadorsri.backend.domain.DetalleComprobante;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import com.facturadorsri.backend.exception.BusinessException;
import org.openpdf.text.*;
import org.openpdf.text.pdf.Barcode128;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * RIDE (representacion impresa del documento electronico) con codigo de
 * barras GS1-128 de la clave de acceso, segun docs/investigacion.md
 * seccion 8. El RIDE es solo la representacion visual; el XML firmado es el
 * comprobante fiscal.
 */
@Service
public class RidePdfService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font FUENTE_NEGRITA = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font FUENTE_NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font FUENTE_PEQUENA = new Font(Font.HELVETICA, 7, Font.NORMAL);

    public byte[] generar(Comprobante comprobante) {
        if (comprobante.getEstado() != EstadoComprobante.AUTORIZADO) {
            throw new BusinessException("Solo se genera el RIDE de comprobantes AUTORIZADOS");
        }
        try {
            Document documento = new Document(PageSize.A5, 20, 20, 20, 20);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            documento.open();

            agregarEncabezado(documento, comprobante);
            agregarDatosComprobante(documento, comprobante, writer);
            agregarDatosComprador(documento, comprobante);
            agregarDetalle(documento, comprobante);
            agregarTotales(documento, comprobante);
            agregarFormasPago(documento, comprobante);
            agregarPie(documento, comprobante);

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new BusinessException("No se pudo generar el RIDE: " + e.getMessage(), e);
        }
    }

    private void agregarEncabezado(Document documento, Comprobante c) throws DocumentException {
        var emisor = c.getEstablecimiento().getEmisor();
        Paragraph razonSocial = new Paragraph(emisor.getRazonSocial(), FUENTE_TITULO);
        documento.add(razonSocial);
        if (emisor.getNombreComercial() != null) {
            documento.add(new Paragraph(emisor.getNombreComercial(), FUENTE_NORMAL));
        }
        documento.add(new Paragraph("RUC: " + emisor.getRuc(), FUENTE_NORMAL));
        documento.add(new Paragraph("Dirección matriz: " + emisor.getDireccionMatriz(), FUENTE_PEQUENA));
        documento.add(new Paragraph("Dirección establecimiento: " + c.getEstablecimiento().getDireccion(),
                FUENTE_PEQUENA));
        documento.add(Chunk.NEWLINE);
    }

    private void agregarDatosComprobante(Document documento, Comprobante c, PdfWriter writer) throws DocumentException {
        String tipo = "01".equals(c.getTipoComprobante()) ? "FACTURA" : "NOTA DE CRÉDITO";
        documento.add(new Paragraph(tipo, FUENTE_TITULO));
        documento.add(new Paragraph("No. %s-%s-%s".formatted(
                c.getEstablecimiento().getCodigo(), c.getPuntoEmision().getCodigo(), c.getSecuencial()),
                FUENTE_NEGRITA));
        documento.add(new Paragraph("Número de autorización: " + c.getNumeroAutorizacion(), FUENTE_NORMAL));
        documento.add(new Paragraph("Fecha y hora de autorización: " + c.getFechaAutorizacion(), FUENTE_NORMAL));
        documento.add(new Paragraph("Ambiente: " + ("1".equals(c.getAmbiente()) ? "PRUEBAS" : "PRODUCCIÓN"),
                FUENTE_NORMAL));
        documento.add(new Paragraph("Clave de acceso:", FUENTE_NEGRITA));
        documento.add(new Paragraph(c.getClaveAcceso(), FUENTE_PEQUENA));

        Barcode128 codigoBarras = new Barcode128();
        codigoBarras.setCode(c.getClaveAcceso());
        codigoBarras.setCodeType(Barcode128.CODE128);
        Image imagenCodigoBarras = codigoBarras.createImageWithBarcode(
                new PdfContentByte(writer), null, null);
        imagenCodigoBarras.scalePercent(85);
        documento.add(imagenCodigoBarras);
        documento.add(Chunk.NEWLINE);
    }

    private void agregarDatosComprador(Document documento, Comprobante c) throws DocumentException {
        documento.add(new Paragraph("Razón social / Nombres: " + c.getCliente().getRazonSocial(), FUENTE_NORMAL));
        documento.add(new Paragraph("Identificación: " + c.getCliente().getIdentificacion(), FUENTE_NORMAL));
        documento.add(new Paragraph("Fecha de emisión: " + c.getFechaEmision().format(FORMATO_FECHA), FUENTE_NORMAL));
        if (c.getCliente().getDireccion() != null) {
            documento.add(new Paragraph("Dirección: " + c.getCliente().getDireccion(), FUENTE_PEQUENA));
        }
        documento.add(Chunk.NEWLINE);
    }

    private void agregarDetalle(Document documento, Comprobante c) throws DocumentException {
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        for (String encabezado : new String[]{"Código", "Descripción", "Cant.", "P. Unit.", "Total"}) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, FUENTE_NEGRITA));
            celda.setGrayFill(0.85f);
            tabla.addCell(celda);
        }
        for (DetalleComprobante detalle : c.getDetalles()) {
            tabla.addCell(new Phrase(detalle.getCodigoPrincipal(), FUENTE_PEQUENA));
            tabla.addCell(new Phrase(detalle.getDescripcion(), FUENTE_PEQUENA));
            tabla.addCell(new Phrase(detalle.getCantidad().toPlainString(), FUENTE_PEQUENA));
            tabla.addCell(new Phrase(detalle.getPrecioUnitario().toPlainString(), FUENTE_PEQUENA));
            tabla.addCell(new Phrase(detalle.getPrecioTotalSinImpuesto().toPlainString(), FUENTE_PEQUENA));
        }
        documento.add(tabla);
        documento.add(Chunk.NEWLINE);
    }

    private void agregarTotales(Document documento, Comprobante c) throws DocumentException {
        documento.add(new Paragraph("Subtotal: $" + c.getTotalSinImpuestos(), FUENTE_NORMAL));
        documento.add(new Paragraph("Descuento: $" + c.getTotalDescuento(), FUENTE_NORMAL));
        documento.add(new Paragraph("VALOR TOTAL: $" + c.getImporteTotal(), FUENTE_TITULO));
        documento.add(Chunk.NEWLINE);
    }

    private void agregarFormasPago(Document documento, Comprobante c) throws DocumentException {
        documento.add(new Paragraph("Formas de pago:", FUENTE_NEGRITA));
        c.getPagos().forEach(p -> {
            try {
                documento.add(new Paragraph("- " + p.getFormaPago() + ": $" + p.getTotal(), FUENTE_NORMAL));
            } catch (DocumentException e) {
                throw new BusinessException("No se pudo agregar la forma de pago al RIDE", e);
            }
        });
    }

    private void agregarPie(Document documento, Comprobante c) throws DocumentException {
        documento.add(Chunk.NEWLINE);
        documento.add(new Paragraph(
                "Este documento es una representación impresa del comprobante electrónico. "
                        + "Consérvelo junto con el XML autorizado.", FUENTE_PEQUENA));
    }
}
