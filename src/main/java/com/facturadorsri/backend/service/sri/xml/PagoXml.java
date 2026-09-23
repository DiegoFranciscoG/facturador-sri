package com.facturadorsri.backend.service.sri.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"formaPago", "total", "plazo", "unidadTiempo"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoXml {

    @XmlElement
    private String formaPago;

    @XmlElement
    private String total;

    @XmlElement
    private String plazo;

    @XmlElement
    private String unidadTiempo;
}
