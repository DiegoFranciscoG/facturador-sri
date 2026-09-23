package com.facturadorsri.backend.service.sri.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"codigoPrincipal", "codigoAuxiliar", "descripcion", "cantidad", "precioUnitario",
        "descuento", "precioTotalSinImpuesto", "impuestos"})
@Getter
@Setter
@NoArgsConstructor
public class DetalleXml {

    @XmlElement
    private String codigoPrincipal;

    @XmlElement
    private String codigoAuxiliar;

    @XmlElement
    private String descripcion;

    @XmlElement
    private String cantidad;

    @XmlElement
    private String precioUnitario;

    @XmlElement
    private String descuento;

    @XmlElement
    private String precioTotalSinImpuesto;

    @XmlElement
    private ImpuestosDetalleXml impuestos;
}
