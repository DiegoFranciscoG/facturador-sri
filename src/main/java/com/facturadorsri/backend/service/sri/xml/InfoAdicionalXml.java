package com.facturadorsri.backend.service.sri.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
public class InfoAdicionalXml {

    @XmlElement(name = "campoAdicional")
    private List<CampoAdicionalXml> campoAdicional = new ArrayList<>();
}
