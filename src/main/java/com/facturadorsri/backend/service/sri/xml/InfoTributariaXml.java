package com.facturadorsri.backend.service.sri.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"ambiente", "tipoEmision", "razonSocial", "nombreComercial", "ruc", "claveAcceso",
        "codDoc", "estab", "ptoEmi", "secuencial", "dirMatriz", "contribuyenteRimpe"})
@Getter
@Setter
@NoArgsConstructor
public class InfoTributariaXml {

    @XmlElement
    private String ambiente;

    @XmlElement
    private String tipoEmision;

    @XmlElement
    private String razonSocial;

    @XmlElement
    private String nombreComercial;

    @XmlElement
    private String ruc;

    @XmlElement
    private String claveAcceso;

    @XmlElement
    private String codDoc;

    @XmlElement
    private String estab;

    @XmlElement
    private String ptoEmi;

    @XmlElement
    private String secuencial;

    @XmlElement
    private String dirMatriz;

    @XmlElement
    private String contribuyenteRimpe;
}
