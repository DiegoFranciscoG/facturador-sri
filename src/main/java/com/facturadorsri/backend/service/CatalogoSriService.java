package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.CatalogoSri;
import com.facturadorsri.backend.domain.enums.NombreCatalogo;
import com.facturadorsri.backend.dto.catalogo.CatalogoItemDto;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.repository.CatalogoSriRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Punto unico de validacion y consulta de los catalogos oficiales de la
 * Ficha Tecnica (versionados en catalogo_sri). Ver docs/modelo-datos.md
 * seccion 3 sobre por que es una tabla generica y no una por catalogo.
 */
@Service
@RequiredArgsConstructor
public class CatalogoSriService {

    private final CatalogoSriRepository catalogoSriRepository;

    public List<CatalogoItemDto> listar(NombreCatalogo catalogo) {
        return catalogoSriRepository.findByCatalogo(catalogo.name()).stream()
                .map(c -> new CatalogoItemDto(c.getCodigo(), c.getDescripcion(), c.getValorNumerico()))
                .toList();
    }

    /** Lanza BusinessException si el codigo no esta vigente hoy en ese catalogo. */
    public CatalogoSri validarVigente(NombreCatalogo catalogo, String codigo) {
        return catalogoSriRepository.buscarVigente(catalogo.name(), codigo, LocalDate.now())
                .orElseThrow(() -> new BusinessException(
                        "El codigo '%s' no esta vigente en el catalogo %s".formatted(codigo, catalogo)));
    }
}
