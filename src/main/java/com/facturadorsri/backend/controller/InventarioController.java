package com.facturadorsri.backend.controller;

import com.facturadorsri.backend.dto.inventario.BodegaRequest;
import com.facturadorsri.backend.dto.inventario.BodegaResponse;
import com.facturadorsri.backend.dto.inventario.MovimientoInventarioRequest;
import com.facturadorsri.backend.dto.inventario.MovimientoInventarioResponse;
import com.facturadorsri.backend.service.BodegaService;
import com.facturadorsri.backend.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final BodegaService bodegaService;

    @PostMapping("/bodegas")
    @ResponseStatus(HttpStatus.CREATED)
    public BodegaResponse crearBodega(@Valid @RequestBody BodegaRequest request) {
        return bodegaService.crear(request);
    }

    @GetMapping("/bodegas")
    public List<BodegaResponse> listarBodegas() {
        return bodegaService.listar();
    }

    @PostMapping("/movimientos")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoInventarioResponse registrarMovimiento(@Valid @RequestBody MovimientoInventarioRequest request) {
        return inventarioService.registrarMovimiento(request);
    }

    @GetMapping("/kardex")
    public List<MovimientoInventarioResponse> kardex(@RequestParam Long productoId, @RequestParam Long bodegaId) {
        return inventarioService.kardex(productoId, bodegaId);
    }

    @GetMapping("/saldo")
    public Map<String, BigDecimal> saldo(@RequestParam Long productoId, @RequestParam Long bodegaId) {
        return Map.of("saldo", inventarioService.saldoActual(productoId, bodegaId));
    }
}
