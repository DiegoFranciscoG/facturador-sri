package com.facturadorsri.backend.service.sri.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"fechaEmision", "dirEstablecimiento", "tipoIdentificacionComprador", "razonSocialComprador",
        "identificacionComprador", "contribuyenteEspecial", "obligadoContabilidad", "codDocModificado",
        "numDocModificado", "fechaEmisionDocSustento", "totalSinImpuestos", "valorModificacion", "moneda",
        "totalConImpuestos", "motivo"})
@Getter
@Setter
@NoArgsConstructor
public class InfoNotaCreditoXml {

    @XmlElement
    private String fechaEmision;

    @XmlElement
    private String dirEstablecimiento;

    @XmlElement
    private String tipoIdentificacionComprador;

    @XmlElement
    private String razonSocialComprador;

    @XmlElement
    private String identificacionComprador;

    @XmlElement
    private String contribuyenteEspecial;

    @XmlElement
    private String obligadoContabilidad;

    @XmlElement
    private String codDocModificado;

    @XmlElement
    private String numDocModificado;

    @XmlElement
    private String fechaEmisionDocSustento;

    @XmlElement
    private String totalSinImpuestos;

    @XmlElement
    private String valorModificacion;

    @XmlElement
    private String moneda;

    @XmlElement
    private TotalConImpuestosXml totalConImpuestos;

    @XmlElement
    private String motivo;
}
