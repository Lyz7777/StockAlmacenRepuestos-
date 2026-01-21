package com.inventario.controller;

import com.inventario.dto.DetalleOrdenCompraDTO;
import com.inventario.dto.OrdenCompraDTO;
import com.inventario.entity.OrdenCompra;
import com.inventario.service.OrdenCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-compra")
@RequiredArgsConstructor
@Tag(name = "Órdenes de Compra", description = "Gestión de órdenes de compra a proveedores")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    @GetMapping
    @Operation(summary = "Listar todas las órdenes de compra con paginación")
    public ResponseEntity<Page<OrdenCompraDTO>> listarTodas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ordenCompraService.listarTodas(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una orden de compra por ID")
    public ResponseEntity<OrdenCompraDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.obtenerPorId(id));
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Listar órdenes pendientes")
    public ResponseEntity<List<OrdenCompraDTO>> listarPendientes() {
        return ResponseEntity.ok(ordenCompraService.listarPendientes());
    }

    @GetMapping("/proveedor/{proveedorId}")
    @Operation(summary = "Listar órdenes por proveedor")
    public ResponseEntity<List<OrdenCompraDTO>> listarPorProveedor(@PathVariable Long proveedorId) {
        return ResponseEntity.ok(ordenCompraService.listarPorProveedor(proveedorId));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva orden de compra")
    public ResponseEntity<OrdenCompraDTO> crear(@Valid @RequestBody OrdenCompraDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenCompraService.crear(dto));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado de una orden")
    public ResponseEntity<OrdenCompraDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam OrdenCompra.EstadoOrden estado) {
        return ResponseEntity.ok(ordenCompraService.actualizarEstado(id, estado));
    }

    @PostMapping("/{id}/recibir")
    @Operation(summary = "Registrar recepción de mercancía")
    public ResponseEntity<OrdenCompraDTO> recibirOrden(
            @PathVariable Long id,
            @RequestBody List<DetalleOrdenCompraDTO> detallesRecibidos) {
        return ResponseEntity.ok(ordenCompraService.recibirOrden(id, detallesRecibidos));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una orden de compra")
    public ResponseEntity<OrdenCompraDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.cancelar(id));
    }

    @GetMapping("/sugerida/{proveedorId}")
    @Operation(summary = "Generar orden de compra sugerida basada en stock bajo")
    public ResponseEntity<OrdenCompraDTO> generarOrdenSugerida(@PathVariable Long proveedorId) {
        return ResponseEntity.ok(ordenCompraService.generarOrdenSugerida(proveedorId));
    }

    @GetMapping("/estadisticas/pendientes")
    @Operation(summary = "Contar órdenes pendientes")
    public ResponseEntity<Long> contarPendientes() {
        return ResponseEntity.ok(ordenCompraService.contarPendientes());
    }
}

