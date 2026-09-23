package com.facturadorsri.backend.controller;

import com.facturadorsri.backend.domain.enums.NombreCatalogo;
import com.facturadorsri.backend.dto.catalogo.CatalogoItemDto;
import com.facturadorsri.backend.service.CatalogoSriService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catalogos oficiales del SRI, para llenar listas desplegables en el frontend. */
@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoSriService catalogoSriService;

    @GetMapping("/{catalogo}")
    public List<CatalogoItemDto> listar(@PathVariable NombreCatalogo catalogo) {
        return catalogoSriService.listar(catalogo);
    }
}
