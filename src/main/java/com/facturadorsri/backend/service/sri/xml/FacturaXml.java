package com.facturadorsri.backend.service.sri.xml;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo JAXB del XML de factura, campos y orden verificados contra un
 * comprobante real de ejemplo (docs/investigacion.md fuente #6, factura
 * version 1.0.0/1.1.0). Cubre unicamente venta domestica sin comercio
 * exterior, reembolsos ni compensaciones (fuera del alcance del MVP).
 *
 * IMPORTANTE: el XSD real de la ficha tecnica no pudo descargarse (ver
 * docs/investigacion.md, WAF de sri.gob.ec). Este modelo es una
 * reconstruccion de buena fe; validar contra el XSD oficial cuando este
 * disponible (docs/investigacion.md seccion 9, riesgo #2).
 */
@XmlRootElement(name = "factura")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"infoTributaria", "infoFactura", "detalles", "infoAdicional"})
@Getter
@Setter
@NoArgsConstructor
public class FacturaXml {

    @XmlAttribute
    private String id = "comprobante";

    @XmlAttribute
    private String version = "1.1.0";

    @XmlElement(name = "infoTributaria")
    private InfoTributariaXml infoTributaria;

    @XmlElement(name = "infoFactura")
    private InfoFacturaXml infoFactura;

    @XmlElement(name = "detalles")
    private DetallesXml detalles;

    @XmlElement(name = "infoAdicional")
    private InfoAdicionalXml infoAdicional;
}
