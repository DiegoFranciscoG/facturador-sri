package com.facturadorsri.backend.service.sri.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"fechaEmision", "dirEstablecimiento", "contribuyenteEspecial", "obligadoContabilidad",
        "tipoIdentificacionComprador", "razonSocialComprador", "identificacionComprador", "direccionComprador",
        "totalSinImpuestos", "totalDescuento", "totalConImpuestos", "propina", "importeTotal", "moneda", "pagos"})
@Getter
@Setter
@NoArgsConstructor
public class InfoFacturaXml {

    @XmlElement
    private String fechaEmision;

    @XmlElement
    private String dirEstablecimiento;

    @XmlElement
    private String contribuyenteEspecial;

    @XmlElement
    private String obligadoContabilidad;

    @XmlElement
    private String tipoIdentificacionComprador;

    @XmlElement
    private String razonSocialComprador;

    @XmlElement
    private String identificacionComprador;

    @XmlElement
    private String direccionComprador;

    @XmlElement
    private String totalSinImpuestos;

    @XmlElement
    private String totalDescuento;

    @XmlElement
    private TotalConImpuestosXml totalConImpuestos;

    @XmlElement
    private String propina;

    @XmlElement
    private String importeTotal;

    @XmlElement
    private String moneda;

    @XmlElement
    private PagosXml pagos;
}
