package com.facturadorsri.backend.service.sri.xml;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo JAXB de nota de credito. Misma reserva que FacturaXml: reconstruido
 * de buena fe a partir de la estructura estandar y estable de la ficha
 * tecnica (docs/investigacion.md seccion 9, riesgo #2), pendiente de
 * contrastar contra el XSD oficial.
 */
@XmlRootElement(name = "notaCredito")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"infoTributaria", "infoNotaCredito", "detalles", "infoAdicional"})
@Getter
@Setter
@NoArgsConstructor
public class NotaCreditoXml {

    @XmlAttribute
    private String id = "comprobante";

    @XmlAttribute
    private String version = "1.1.0";

    @XmlElement(name = "infoTributaria")
    private InfoTributariaXml infoTributaria;

    @XmlElement(name = "infoNotaCredito")
    private InfoNotaCreditoXml infoNotaCredito;

    @XmlElement(name = "detalles")
    private DetallesXml detalles;

    @XmlElement(name = "infoAdicional")
    private InfoAdicionalXml infoAdicional;
}
