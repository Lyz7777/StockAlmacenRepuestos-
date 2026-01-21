package com.inventario.controller;

import com.inventario.dto.ProductoBusquedaDTO;
import com.inventario.dto.ProductoDTO;
import com.inventario.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión de productos e inventario")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar todos los productos con paginación")
    public ResponseEntity<Page<ProductoDTO>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        return ResponseEntity.ok(productoService.listarTodos(page, size, sortBy, sortDirection));
    }

    @GetMapping("/{codigoBarras}")
    @Operation(summary = "Obtener un producto por código de barras")
    public ResponseEntity<ProductoDTO> obtenerPorCodigoBarras(@PathVariable String codigoBarras) {
        return ResponseEntity.ok(productoService.obtenerPorCodigoBarras(codigoBarras));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo producto")
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    @PutMapping("/{codigoBarras}")
    @Operation(summary = "Actualizar un producto existente")
    public ResponseEntity<ProductoDTO> actualizar(
            @PathVariable String codigoBarras,
            @Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(codigoBarras, dto));
    }

    @DeleteMapping("/{codigoBarras}")
    @Operation(summary = "Eliminar un producto (eliminación lógica)")
    public ResponseEntity<Void> eliminar(@PathVariable String codigoBarras) {
        productoService.eliminar(codigoBarras);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/busqueda-avanzada")
    @Operation(summary = "Búsqueda avanzada de productos con múltiples filtros")
    public ResponseEntity<Page<ProductoDTO>> busquedaAvanzada(@RequestBody ProductoBusquedaDTO filtros) {
        return ResponseEntity.ok(productoService.busquedaAvanzada(filtros));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Búsqueda general de productos para autocompletado")
    public ResponseEntity<List<ProductoDTO>> busquedaGeneral(@RequestParam String texto) {
        return ResponseEntity.ok(productoService.busquedaGeneral(texto));
    }

    @GetMapping("/stock-bajo")
    @Operation(summary = "Listar productos con stock bajo")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosStockBajo() {
        return ResponseEntity.ok(productoService.obtenerProductosStockBajo());
    }

    @GetMapping("/agotados")
    @Operation(summary = "Listar productos agotados")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosAgotados() {
        return ResponseEntity.ok(productoService.obtenerProductosAgotados());
    }

    @PostMapping("/{codigoBarras}/entrada")
    @Operation(summary = "Registrar entrada de stock")
    public ResponseEntity<ProductoDTO> registrarEntrada(
            @PathVariable String codigoBarras,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(productoService.ajustarStock(codigoBarras, cantidad,
                motivo != null ? motivo : "Entrada de mercancía", true));
    }

    @PostMapping("/{codigoBarras}/ajuste")
    @Operation(summary = "Realizar ajuste de inventario")
    public ResponseEntity<ProductoDTO> ajustarStock(
            @PathVariable String codigoBarras,
            @RequestParam Integer cantidad,
            @RequestParam String motivo,
            @RequestParam boolean esEntrada) {
        return ResponseEntity.ok(productoService.ajustarStock(codigoBarras, cantidad, motivo, esEntrada));
    }

    @GetMapping("/autocompletar/marcas")
    @Operation(summary = "Autocompletado de marcas")
    public ResponseEntity<List<String>> autocompletarMarcas(@RequestParam String texto) {
        return ResponseEntity.ok(productoService.autocompletarMarcas(texto));
    }

    @GetMapping("/autocompletar/modelos")
    @Operation(summary = "Autocompletado de modelos compatibles")
    public ResponseEntity<List<String>> autocompletarModelos(@RequestParam String texto) {
        return ResponseEntity.ok(productoService.autocompletarModelos(texto));
    }

    @GetMapping("/generar-codigo")
    @Operation(summary = "Generar un nuevo código de barras único")
    public ResponseEntity<Map<String, String>> generarCodigoBarras() {
        String codigo = productoService.generarCodigoBarras();
        Map<String, String> response = new HashMap<>();
        response.put("codigoBarras", codigo);
        response.put("imagenBase64", productoService.generarImagenCodigoBarras(codigo));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{codigoBarras}/imagen-codigo")
    @Operation(summary = "Obtener imagen del código de barras en Base64")
    public ResponseEntity<Map<String, String>> obtenerImagenCodigoBarras(@PathVariable String codigoBarras) {
        Map<String, String> response = new HashMap<>();
        response.put("codigoBarras", codigoBarras);
        response.put("imagenBase64", productoService.generarImagenCodigoBarras(codigoBarras));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas de productos")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("stockBajo", productoService.contarProductosStockBajo());
        stats.put("agotados", productoService.contarProductosAgotados());
        return ResponseEntity.ok(stats);
    }
}

