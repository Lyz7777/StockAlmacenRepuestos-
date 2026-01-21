package com.inventario.controller;

import com.inventario.dto.MovimientoInventarioDTO;
import com.inventario.entity.MovimientoInventario;
import com.inventario.service.MovimientoInventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Tag(name = "Movimientos de Inventario", description = "Gestión de movimientos de inventario")
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoService;

    @GetMapping
    @Operation(summary = "Listar todos los movimientos con paginación")
    public ResponseEntity<Page<MovimientoInventarioDTO>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(movimientoService.listarTodos(page, size));
    }

    @GetMapping("/producto/{codigoBarras}")
    @Operation(summary = "Listar movimientos de un producto")
    public ResponseEntity<List<MovimientoInventarioDTO>> listarPorProducto(@PathVariable String codigoBarras) {
        return ResponseEntity.ok(movimientoService.listarPorProducto(codigoBarras));
    }

    @GetMapping("/producto/{codigoBarras}/paginado")
    @Operation(summary = "Listar movimientos de un producto con paginación")
    public ResponseEntity<Page<MovimientoInventarioDTO>> listarPorProductoPaginado(
            @PathVariable String codigoBarras,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(movimientoService.listarPorProductoPaginado(codigoBarras, page, size));
    }

    @GetMapping("/por-fecha")
    @Operation(summary = "Listar movimientos por rango de fechas")
    public ResponseEntity<List<MovimientoInventarioDTO>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(movimientoService.listarPorFecha(fechaInicio, fechaFin));
    }

    @GetMapping("/por-tipo/{tipo}")
    @Operation(summary = "Listar movimientos por tipo")
    public ResponseEntity<List<MovimientoInventarioDTO>> listarPorTipo(
            @PathVariable MovimientoInventario.TipoMovimiento tipo) {
        return ResponseEntity.ok(movimientoService.listarPorTipo(tipo));
    }

    @PostMapping("/entrada")
    @Operation(summary = "Registrar entrada de mercancía")
    public ResponseEntity<MovimientoInventarioDTO> registrarEntrada(
            @RequestParam String codigoBarras,
            @RequestParam Integer cantidad,
            @RequestParam(required = false, defaultValue = "Entrada de mercancía") String motivo) {
        return ResponseEntity.ok(movimientoService.registrarEntrada(codigoBarras, cantidad, motivo));
    }

    @PostMapping("/ajuste")
    @Operation(summary = "Registrar ajuste de inventario")
    public ResponseEntity<MovimientoInventarioDTO> registrarAjuste(
            @RequestParam String codigoBarras,
            @RequestParam Integer cantidad,
            @RequestParam String motivo,
            @RequestParam boolean esPositivo) {
        return ResponseEntity.ok(movimientoService.registrarAjuste(codigoBarras, cantidad, motivo, esPositivo));
    }

    @GetMapping("/ultimos")
    @Operation(summary = "Obtener últimos movimientos")
    public ResponseEntity<List<MovimientoInventarioDTO>> obtenerUltimosMovimientos(
            @RequestParam(defaultValue = "10") int cantidad) {
        return ResponseEntity.ok(movimientoService.obtenerUltimosMovimientos(cantidad));
    }
}

