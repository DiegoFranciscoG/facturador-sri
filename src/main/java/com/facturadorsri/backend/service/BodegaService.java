package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.Bodega;
import com.facturadorsri.backend.domain.Establecimiento;
import com.facturadorsri.backend.dto.inventario.BodegaRequest;
import com.facturadorsri.backend.dto.inventario.BodegaResponse;
import com.facturadorsri.backend.exception.ResourceNotFoundException;
import com.facturadorsri.backend.repository.BodegaRepository;
import com.facturadorsri.backend.repository.EstablecimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BodegaService {

    private final BodegaRepository bodegaRepository;
    private final EstablecimientoRepository establecimientoRepository;

    public BodegaResponse crear(BodegaRequest request) {
        Establecimiento establecimiento = null;
        if (request.establecimientoId() != null) {
            establecimiento = establecimientoRepository.findById(request.establecimientoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Establecimiento no encontrado: " + request.establecimientoId()));
        }
        Bodega bodega = Bodega.builder()
                .nombre(request.nombre())
                .establecimiento(establecimiento)
                .activa(true)
                .build();
        return aResponse(bodegaRepository.save(bodega));
    }

    @Transactional(readOnly = true)
    public List<BodegaResponse> listar() {
        return bodegaRepository.findAll().stream().map(this::aResponse).toList();
    }

    private BodegaResponse aResponse(Bodega b) {
        return new BodegaResponse(b.getId(), b.getNombre(),
                b.getEstablecimiento() != null ? b.getEstablecimiento().getId() : null, b.isActiva());
    }
}
