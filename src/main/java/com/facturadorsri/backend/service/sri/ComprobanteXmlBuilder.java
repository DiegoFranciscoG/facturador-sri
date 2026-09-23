package com.facturadorsri.backend.service.sri;

import com.facturadorsri.backend.domain.Comprobante;
import com.facturadorsri.backend.domain.DetalleComprobante;
import com.facturadorsri.backend.domain.enums.TipoComprobante;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.service.sri.xml.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Construye y serializa el XML sin firmar de factura o nota de credito a
 * partir del comprobante ya persistido. Ver docs/investigacion.md seccion 6
 * y service/sri/xml/*.java para las reservas sobre el XSD reconstruido.
 */
@Component
public class ComprobanteXmlBuilder {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String construirFacturaXml(Comprobante comprobante) {
        if (!TipoComprobante.FACTURA.getCodigo().equals(comprobante.getTipoComprobante())) {
            throw new IllegalArgumentException("El comprobante no es una factura");
        }
        FacturaXml factura = new FacturaXml();
        factura.setInfoTributaria(construirInfoTributaria(comprobante));
        factura.setInfoFactura(construirInfoFactura(comprobante));
        factura.setDetalles(construirDetalles(comprobante));
        factura.setInfoAdicional(construirInfoAdicional(comprobante));
        return marshal(factura, FacturaXml.class);
    }

    public String construirNotaCreditoXml(Comprobante comprobante) {
        if (!TipoComprobante.NOTA_CREDITO.getCodigo().equals(comprobante.getTipoComprobante())) {
            throw new IllegalArgumentException("El comprobante no es una nota de credito");
        }
        Comprobante original = comprobante.getComprobanteRelacionado();
        if (original == null) {
            throw new BusinessException("La nota de credito debe referenciar el comprobante que modifica");
        }

        NotaCreditoXml notaCredito = new NotaCreditoXml();
        notaCredito.setInfoTributaria(construirInfoTributaria(comprobante));

        InfoNotaCreditoXml info = new InfoNotaCreditoXml();
        info.setFechaEmision(comprobante.getFechaEmision().format(FORMATO_FECHA));
        info.setDirEstablecimiento(comprobante.getEstablecimiento().getDireccion());
        info.setTipoIdentificacionComprador(comprobante.getCliente().getTipoIdentificacion());
        info.setRazonSocialComprador(comprobante.getCliente().getRazonSocial());
        info.setIdentificacionComprador(comprobante.getCliente().getIdentificacion());
        info.setContribuyenteEspecial(comprobante.getEstablecimiento().getEmisor().getContribuyenteEspecial());
        info.setObligadoContabilidad(
                comprobante.getEstablecimiento().getEmisor().isObligadoContabilidad() ? "SI" : "NO");
        info.setCodDocModificado(original.getTipoComprobante());
        info.setNumDocModificado("%s-%s-%s".formatted(
                original.getEstablecimiento().getCodigo(), original.getPuntoEmision().getCodigo(),
                original.getSecuencial()));
        info.setFechaEmisionDocSustento(original.getFechaEmision().format(FORMATO_FECHA));
        info.setTotalSinImpuestos(dosDecimales(comprobante.getTotalSinImpuestos()));
        info.setValorModificacion(dosDecimales(comprobante.getImporteTotal()));
        info.setMoneda("DOLAR");
        info.setTotalConImpuestos(construirTotalConImpuestos(comprobante));
        info.setMotivo(comprobante.getMotivo());
        notaCredito.setInfoNotaCredito(info);

        notaCredito.setDetalles(construirDetalles(comprobante));
        notaCredito.setInfoAdicional(construirInfoAdicional(comprobante));
        return marshal(notaCredito, NotaCreditoXml.class);
    }

    private InfoTributariaXml construirInfoTributaria(Comprobante comprobante) {
        InfoTributariaXml info = new InfoTributariaXml();
        info.setAmbiente(comprobante.getAmbiente());
        info.setTipoEmision(comprobante.getTipoEmision());
        info.setRazonSocial(comprobante.getEstablecimiento().getEmisor().getRazonSocial());
        info.setNombreComercial(comprobante.getEstablecimiento().getEmisor().getNombreComercial());
        info.setRuc(comprobante.getEstablecimiento().getEmisor().getRuc());
        info.setClaveAcceso(comprobante.getClaveAcceso());
        info.setCodDoc(comprobante.getTipoComprobante());
        info.setEstab(comprobante.getEstablecimiento().getCodigo());
        info.setPtoEmi(comprobante.getPuntoEmision().getCodigo());
        info.setSecuencial(comprobante.getSecuencial());
        info.setDirMatriz(comprobante.getEstablecimiento().getEmisor().getDireccionMatriz());
        if (comprobante.getEstablecimiento().getEmisor().getRegimenRimpe() != null) {
            info.setContribuyenteRimpe(switch (comprobante.getEstablecimiento().getEmisor().getRegimenRimpe()) {
                case CONTRIBUYENTE_NEGOCIO_POPULAR -> "CONTRIBUYENTE NEGOCIO POPULAR - REGIMEN RIMPE";
                case CONTRIBUYENTE_REGIMEN_RIMPE -> "CONTRIBUYENTE REGIMEN RIMPE";
            });
        }
        return info;
    }

    private InfoFacturaXml construirInfoFactura(Comprobante comprobante) {
        InfoFacturaXml info = new InfoFacturaXml();
        info.setFechaEmision(comprobante.getFechaEmision().format(FORMATO_FECHA));
        info.setDirEstablecimiento(comprobante.getEstablecimiento().getDireccion());
        info.setContribuyenteEspecial(comprobante.getEstablecimiento().getEmisor().getContribuyenteEspecial());
        info.setObligadoContabilidad(
                comprobante.getEstablecimiento().getEmisor().isObligadoContabilidad() ? "SI" : "NO");
        info.setTipoIdentificacionComprador(comprobante.getCliente().getTipoIdentificacion());
        info.setRazonSocialComprador(comprobante.getCliente().getRazonSocial());
        info.setIdentificacionComprador(comprobante.getCliente().getIdentificacion());
        info.setDireccionComprador(comprobante.getCliente().getDireccion());
        info.setTotalSinImpuestos(dosDecimales(comprobante.getTotalSinImpuestos()));
        info.setTotalDescuento(dosDecimales(comprobante.getTotalDescuento()));
        info.setTotalConImpuestos(construirTotalConImpuestos(comprobante));
        info.setPropina("0.00");
        info.setImporteTotal(dosDecimales(comprobante.getImporteTotal()));
        info.setMoneda("DOLAR");

        PagosXml pagos = new PagosXml();
        comprobante.getPagos().forEach(p -> pagos.getPago().add(new PagoXml(
                p.getFormaPago(), dosDecimales(p.getTotal()),
                p.getPlazo() != null ? p.getPlazo().toString() : null, p.getUnidadTiempo())));
        info.setPagos(pagos);
        return info;
    }

    private TotalConImpuestosXml construirTotalConImpuestos(Comprobante comprobante) {
        TotalConImpuestosXml total = new TotalConImpuestosXml();
        record Clave(String codigo, String codigoPorcentaje, BigDecimal tarifa) {
        }
        var agregados = new java.util.LinkedHashMap<Clave, BigDecimal[]>();
        for (DetalleComprobante detalle : comprobante.getDetalles()) {
            for (var impuesto : detalle.getImpuestos()) {
                Clave clave = new Clave(impuesto.getCodigoImpuesto(), impuesto.getCodigoPorcentaje(), impuesto.getTarifa());
                agregados.computeIfAbsent(clave, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                agregados.get(clave)[0] = agregados.get(clave)[0].add(impuesto.getBaseImponible());
                agregados.get(clave)[1] = agregados.get(clave)[1].add(impuesto.getValor());
            }
        }
        agregados.forEach((clave, sumas) -> total.getTotalImpuesto().add(new TotalImpuestoXml(
                clave.codigo(), clave.codigoPorcentaje(), dosDecimales(sumas[0]), dosDecimales(clave.tarifa()),
                dosDecimales(sumas[1]))));
        return total;
    }

    private DetallesXml construirDetalles(Comprobante comprobante) {
        DetallesXml detalles = new DetallesXml();
        for (DetalleComprobante detalle : comprobante.getDetalles()) {
            DetalleXml detalleXml = new DetalleXml();
            detalleXml.setCodigoPrincipal(detalle.getCodigoPrincipal());
            detalleXml.setDescripcion(detalle.getDescripcion());
            detalleXml.setCantidad(seisDecimales(detalle.getCantidad()));
            detalleXml.setPrecioUnitario(seisDecimales(detalle.getPrecioUnitario()));
            detalleXml.setDescuento(dosDecimales(detalle.getDescuento()));
            detalleXml.setPrecioTotalSinImpuesto(dosDecimales(detalle.getPrecioTotalSinImpuesto()));

            ImpuestosDetalleXml impuestos = new ImpuestosDetalleXml();
            detalle.getImpuestos().forEach(i -> impuestos.getImpuesto().add(new ImpuestoDetalleXml(
                    i.getCodigoImpuesto(), i.getCodigoPorcentaje(), dosDecimales(i.getTarifa()),
                    dosDecimales(i.getBaseImponible()), dosDecimales(i.getValor()))));
            detalleXml.setImpuestos(impuestos);
            detalles.getDetalle().add(detalleXml);
        }
        return detalles;
    }

    private InfoAdicionalXml construirInfoAdicional(Comprobante comprobante) {
        List<CampoAdicionalXml> campos = new ArrayList<>();
        if (comprobante.getCliente().getEmail() != null && !comprobante.getCliente().getEmail().isBlank()) {
            campos.add(new CampoAdicionalXml("Email", comprobante.getCliente().getEmail()));
        }
        if (comprobante.getCliente().getTelefono() != null && !comprobante.getCliente().getTelefono().isBlank()) {
            campos.add(new CampoAdicionalXml("Telefono", comprobante.getCliente().getTelefono()));
        }
        if (campos.isEmpty()) {
            return null;
        }
        InfoAdicionalXml infoAdicional = new InfoAdicionalXml();
        infoAdicional.setCampoAdicional(campos);
        return infoAdicional;
    }

    private <T> String marshal(T objeto, Class<T> tipo) {
        try {
            JAXBContext context = JAXBContext.newInstance(tipo);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(objeto, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new BusinessException("No se pudo generar el XML del comprobante: " + e.getMessage(), e);
        }
    }

    private String dosDecimales(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String seisDecimales(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(6, RoundingMode.HALF_UP).toPlainString();
    }
}
