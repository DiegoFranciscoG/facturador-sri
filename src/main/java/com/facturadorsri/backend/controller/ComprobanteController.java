package com.facturadorsri.backend.controller;

import com.facturadorsri.backend.domain.Comprobante;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import com.facturadorsri.backend.dto.comprobante.ComprobanteResponse;
import com.facturadorsri.backend.dto.comprobante.NotaCreditoRequest;
import com.facturadorsri.backend.dto.comprobante.VentaRequest;
import com.facturadorsri.backend.exception.ResourceNotFoundException;
import com.facturadorsri.backend.repository.ComprobanteRepository;
import com.facturadorsri.backend.service.NotaCreditoService;
import com.facturadorsri.backend.service.VentaService;
import com.facturadorsri.backend.service.ride.RidePdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

    private final VentaService ventaService;
    private final NotaCreditoService notaCreditoService;
    private final ComprobanteRepository comprobanteRepository;
    private final RidePdfService ridePdfService;

    @PostMapping("/facturas")
    @ResponseStatus(HttpStatus.CREATED)
    public ComprobanteResponse crearFactura(@Valid @RequestBody VentaRequest request) {
        return ventaService.crearFactura(request);
    }

    @PostMapping("/notas-credito")
    @ResponseStatus(HttpStatus.CREATED)
    public ComprobanteResponse crearNotaCredito(@Valid @RequestBody NotaCreditoRequest request) {
        return notaCreditoService.crear(request);
    }

    @GetMapping
    public Page<ComprobanteResponse> listar(@RequestParam(required = false) EstadoComprobante estado,
                                             Pageable pageable) {
        Page<Comprobante> pagina = estado != null
                ? comprobanteRepository.findByEstado(estado, pageable)
                : comprobanteRepository.findAll(pageable);
        return pagina.map(this::aResponse);
    }

    @GetMapping("/{id}")
    public ComprobanteResponse obtener(@PathVariable Long id) {
        return aResponse(buscarOLanzar(id));
    }

    @GetMapping(value = "/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> obtenerXml(@PathVariable Long id) {
        Comprobante comprobante = buscarOLanzar(id);
        String xml = comprobante.getXmlAutorizacion() != null
                ? comprobante.getXmlAutorizacion() : comprobante.getXmlFirmado();
        if (xml == null) {
            throw new ResourceNotFoundException("El comprobante aun no tiene XML generado");
        }
        return ResponseEntity.ok(xml);
    }

    @GetMapping(value = "/{id}/ride", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> obtenerRide(@PathVariable Long id) {
        Comprobante comprobante = buscarOLanzar(id);
        byte[] pdf = ridePdfService.generar(comprobante);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"ride-" + comprobante.getClaveAcceso() + ".pdf\"")
                .body(pdf);
    }

    private Comprobante buscarOLanzar(Long id) {
        return comprobanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado: " + id));
    }

    private ComprobanteResponse aResponse(Comprobante c) {
        return new ComprobanteResponse(c.getId(), c.getClaveAcceso(), c.getTipoComprobante(), c.getSecuencial(),
                c.getFechaEmision(), c.getEstado(), c.getTotalSinImpuestos(), c.getTotalDescuento(),
                c.getImporteTotal(), c.getNumeroAutorizacion(), c.getFechaAutorizacion(),
                c.getCliente().getRazonSocial());
    }
}
