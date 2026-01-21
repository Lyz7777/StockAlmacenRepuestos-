package com.inventario.controller;

import com.inventario.dto.VentaDTO;
import com.inventario.service.ReporteService;
import com.inventario.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión de ventas")
public class VentaController {

    private final VentaService ventaService;
    private final ReporteService reporteService;

    @GetMapping
    @Operation(summary = "Listar todas las ventas con paginación")
    public ResponseEntity<Page<VentaDTO>> listarTodas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ventaService.listarTodas(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta por ID")
    public ResponseEntity<VentaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @GetMapping("/hoy")
    @Operation(summary = "Listar ventas del día")
    public ResponseEntity<List<VentaDTO>> listarVentasHoy() {
        return ResponseEntity.ok(ventaService.listarVentasHoy());
    }

    @GetMapping("/por-fecha")
    @Operation(summary = "Listar ventas por rango de fechas")
    public ResponseEntity<List<VentaDTO>> listarVentasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(ventaService.listarVentasPorFecha(fechaInicio, fechaFin));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva venta")
    public ResponseEntity<VentaDTO> crearVenta(@Valid @RequestBody VentaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.crearVenta(dto));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una venta y devolver stock")
    public ResponseEntity<VentaDTO> cancelarVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.cancelarVenta(id));
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas de ventas del día")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("ventasHoy", ventaService.contarVentasHoy());
        stats.put("totalHoy", ventaService.obtenerTotalVentasHoy());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/por-producto/{codigoProducto}")
    @Operation(summary = "Obtener historial de ventas de un producto")
    public ResponseEntity<List<VentaDTO>> obtenerVentasPorProducto(@PathVariable String codigoProducto) {
        return ResponseEntity.ok(ventaService.obtenerVentasPorProducto(codigoProducto));
    }

    @GetMapping("/{id}/ticket")
    @Operation(summary = "Generar ticket de venta en PDF")
    public ResponseEntity<byte[]> generarTicket(@PathVariable Long id) throws IOException {
        VentaDTO venta = ventaService.obtenerPorId(id);
        byte[] pdf = reporteService.generarPdfTicketVenta(venta);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ticket-venta-" + id + ".pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}

