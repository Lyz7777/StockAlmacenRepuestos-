package com.inventario.controller;

import com.inventario.dto.DashboardDTO;
import com.inventario.dto.ReporteInventarioDTO;
import com.inventario.dto.ReporteVentasDTO;
import com.inventario.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Generación de reportes y dashboard")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener datos del dashboard")
    public ResponseEntity<DashboardDTO> obtenerDashboard() {
        return ResponseEntity.ok(reporteService.obtenerDashboard());
    }

    @GetMapping("/inventario")
    @Operation(summary = "Generar reporte de inventario")
    public ResponseEntity<ReporteInventarioDTO> generarReporteInventario() {
        return ResponseEntity.ok(reporteService.generarReporteInventario());
    }

    @GetMapping("/ventas")
    @Operation(summary = "Generar reporte de ventas por período")
    public ResponseEntity<ReporteVentasDTO> generarReporteVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.generarReporteVentas(fechaInicio, fechaFin));
    }

    @GetMapping("/inventario/pdf")
    @Operation(summary = "Generar reporte de inventario en PDF")
    public ResponseEntity<byte[]> generarPdfInventario() throws IOException {
        byte[] pdf = reporteService.generarPdfInventario();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte-inventario.pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/ventas/pdf")
    @Operation(summary = "Generar reporte de ventas en PDF")
    public ResponseEntity<byte[]> generarPdfVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) throws IOException {
        byte[] pdf = reporteService.generarPdfVentas(fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "reporte-ventas-" + fechaInicio + "-a-" + fechaFin + ".pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
package com.inventario.service;

import com.inventario.dto.*;
import com.inventario.entity.Producto;
import com.inventario.repository.*;
import com.inventario.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final CategoriaRepository categoriaRepository;
    private final PdfGenerator pdfGenerator;

    public DashboardDTO obtenerDashboard() {
        // Estadísticas de productos
        long totalProductos = productoRepository.count();
        long productosStockBajo = productoRepository.countProductosStockBajo();
        long productosAgotados = productoRepository.countProductosAgotados();

        // Estadísticas de ventas
        Long ventasHoy = ventaRepository.countVentasHoy();
        BigDecimal totalVentasHoy = ventaRepository.sumTotalVentasHoy();

        // Ventas de la semana
        LocalDateTime inicioSemana = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime finSemana = LocalDateTime.now();
        BigDecimal totalVentasSemana = ventaRepository.sumTotalVentasByFecha(inicioSemana, finSemana);

        // Ventas del mes
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        BigDecimal totalVentasMes = ventaRepository.sumTotalVentasByFecha(inicioMes, finSemana);

        // Valor del inventario
        BigDecimal valorInventario = calcularValorInventario();

        // Productos más vendidos (últimos 30 días)
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        List<Object[]> masVendidos = detalleVentaRepository.findProductosMasVendidosPorPeriodo(
                hace30Dias, LocalDateTime.now());
        List<ProductoMasVendidoDTO> productosMasVendidos = masVendidos.stream()
                .limit(5)
                .map(obj -> ProductoMasVendidoDTO.builder()
                        .codigoBarras((String) obj[0])
                        .nombre((String) obj[1])
                        .cantidadVendida(((Number) obj[2]).longValue())
                        .build())
                .collect(Collectors.toList());

        // Productos con stock crítico
        List<ProductoDTO> productosStockCritico = productoRepository.findProductosStockBajo()
                .stream()
                .limit(10)
                .map(this::convertirProductoADTO)
                .collect(Collectors.toList());

        return DashboardDTO.builder()
                .totalProductos(totalProductos)
                .productosStockBajo(productosStockBajo)
                .productosAgotados(productosAgotados)
                .ventasHoy(ventasHoy != null ? ventasHoy : 0L)
                .totalVentasHoy(totalVentasHoy != null ? totalVentasHoy : BigDecimal.ZERO)
                .totalVentasSemana(totalVentasSemana != null ? totalVentasSemana : BigDecimal.ZERO)
                .totalVentasMes(totalVentasMes != null ? totalVentasMes : BigDecimal.ZERO)
                .valorInventario(valorInventario)
                .productosMasVendidos(productosMasVendidos)
                .productosStockCritico(productosStockCritico)
                .build();
    }

    public ReporteInventarioDTO generarReporteInventario() {
        List<Producto> productos = productoRepository.findByActivoTrue();

        long totalProductos = productos.size();
        long productosConStock = productos.stream().filter(p -> p.getStockActual() > 0).count();
        long productosAgotados = productos.stream().filter(p -> p.getStockActual() == 0).count();
        long productosStockBajo = productos.stream().filter(Producto::isStockBajo).count();

        BigDecimal valorTotal = productos.stream()
                .map(p -> p.getPrecioVenta().multiply(BigDecimal.valueOf(p.getStockActual())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Inventario por categoría
        List<InventarioPorCategoriaDTO> inventarioPorCategoria = categoriaRepository.findByActivoTrue()
                .stream()
                .map(cat -> {
                    List<Producto> productosCategoria = productos.stream()
                            .filter(p -> p.getCategoria() != null && p.getCategoria().getId().equals(cat.getId()))
                            .collect(Collectors.toList());

                    long cantidadProductos = productosCategoria.size();
                    long totalUnidades = productosCategoria.stream()
                            .mapToLong(Producto::getStockActual)
                            .sum();
                    BigDecimal valorCategoria = productosCategoria.stream()
                            .map(p -> p.getPrecioVenta().multiply(BigDecimal.valueOf(p.getStockActual())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return InventarioPorCategoriaDTO.builder()
                            .categoriaId(cat.getId())
                            .categoriaNombre(cat.getNombre())
                            .cantidadProductos(cantidadProductos)
                            .totalUnidades(totalUnidades)
                            .valorTotal(valorCategoria)
                            .build();
                })
                .filter(inv -> inv.getCantidadProductos() > 0)
                .collect(Collectors.toList());

        return ReporteInventarioDTO.builder()
                .totalProductos(totalProductos)
                .productosConStock(productosConStock)
                .productosAgotados(productosAgotados)
                .productosStockBajo(productosStockBajo)
                .valorTotalInventario(valorTotal)
                .inventarioPorCategoria(inventarioPorCategoria)
                .build();
    }

    public ReporteVentasDTO generarReporteVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        BigDecimal totalVentas = ventaRepository.sumTotalVentasByFecha(inicio, fin);
        if (totalVentas == null) totalVentas = BigDecimal.ZERO;

        List<Object[]> ventasDiariasRaw = ventaRepository.getVentasDiarias(inicio, fin);
        List<VentaDiariaDTO> ventasDiarias = ventasDiariasRaw.stream()
                .map(obj -> VentaDiariaDTO.builder()
                        .fecha(((java.sql.Date) obj[0]).toLocalDate())
                        .total((BigDecimal) obj[1])
                        .cantidadVentas(1L) // Se ajustaría con query más específica
                        .build())
                .collect(Collectors.toList());

        long cantidadVentas = ventaRepository.findByFechaHoraBetween(inicio, fin).size();

        // Productos más vendidos en el período
        List<Object[]> masVendidos = detalleVentaRepository.findProductosMasVendidosPorPeriodo(inicio, fin);
        List<ProductoMasVendidoDTO> productosMasVendidos = masVendidos.stream()
                .limit(10)
                .map(obj -> ProductoMasVendidoDTO.builder()
                        .codigoBarras((String) obj[0])
                        .nombre((String) obj[1])
                        .cantidadVendida(((Number) obj[2]).longValue())
                        .build())
                .collect(Collectors.toList());

        long cantidadProductosVendidos = productosMasVendidos.stream()
                .mapToLong(ProductoMasVendidoDTO::getCantidadVendida)
                .sum();

        BigDecimal promedioVenta = cantidadVentas > 0
                ? totalVentas.divide(BigDecimal.valueOf(cantidadVentas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ReporteVentasDTO.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalVentas(totalVentas)
                .cantidadVentas(cantidadVentas)
                .cantidadProductosVendidos(cantidadProductosVendidos)
                .promedioVenta(promedioVenta)
                .ventasDiarias(ventasDiarias)
                .productosMasVendidos(productosMasVendidos)
                .build();
    }

    public byte[] generarPdfInventario() throws IOException {
        List<ProductoDTO> productos = productoRepository.findByActivoTrue().stream()
                .map(this::convertirProductoADTO)
                .collect(Collectors.toList());
        ReporteInventarioDTO resumen = generarReporteInventario();
        return pdfGenerator.generarReporteInventario(productos, resumen);
    }

    public byte[] generarPdfVentas(LocalDate fechaInicio, LocalDate fechaFin) throws IOException {
        ReporteVentasDTO reporte = generarReporteVentas(fechaInicio, fechaFin);
        return pdfGenerator.generarReporteVentas(reporte);
    }

    public byte[] generarPdfTicketVenta(VentaDTO venta) throws IOException {
        return pdfGenerator.generarTicketVenta(venta);
    }

    private BigDecimal calcularValorInventario() {
        return productoRepository.findByActivoTrue().stream()
                .map(p -> p.getPrecioVenta().multiply(BigDecimal.valueOf(p.getStockActual())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ProductoDTO convertirProductoADTO(Producto producto) {
        ProductoDTO dto = ProductoDTO.builder()
                .codigoBarras(producto.getCodigoBarras())
                .codigoInterno(producto.getCodigoInterno())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .marca(producto.getMarca())
                .modeloCompatible(producto.getModeloCompatible())
                .precioVenta(producto.getPrecioVenta())
                .stockActual(producto.getStockActual())
                .stockMinimo(producto.getStockMinimo())
                .fechaIngreso(producto.getFechaIngreso())
                .fechaUltimaVenta(producto.getFechaUltimaVenta())
                .ubicacion(producto.getUbicacion())
                .imagenUrl(producto.getImagenUrl())
                .activo(producto.getActivo())
                .stockBajo(producto.isStockBajo())
                .agotado(producto.isAgotado())
                .build();

        if (producto.getCategoria() != null) {
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }

        if (producto.getProveedor() != null) {
            dto.setProveedorId(producto.getProveedor().getId());
            dto.setProveedorNombre(producto.getProveedor().getNombre());
        }

        return dto;
    }
}

