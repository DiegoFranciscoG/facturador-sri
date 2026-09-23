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
@XmlType(propOrder = {"codigo", "codigoPorcentaje", "tarifa", "baseImponible", "valor"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImpuestoDetalleXml {

    @XmlElement
    private String codigo;

    @XmlElement
    private String codigoPorcentaje;

    @XmlElement
    private String tarifa;

    @XmlElement
    private String baseImponible;

    @XmlElement
    private String valor;
}
