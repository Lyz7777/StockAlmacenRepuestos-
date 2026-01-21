package com.inventario.service;

import com.inventario.dto.DetalleVentaDTO;
import com.inventario.dto.VentaDTO;
import com.inventario.entity.*;
import com.inventario.exception.BadRequestException;
import com.inventario.exception.ResourceNotFoundException;
import com.inventario.exception.StockInsuficienteException;
import com.inventario.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public Page<VentaDTO> listarTodas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaHora"));
        return ventaRepository.findAll(pageable).map(this::convertirADTO);
    }

    public VentaDTO obtenerPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", "id", id));
        return convertirADTO(venta);
    }

    public List<VentaDTO> listarVentasHoy() {
        return ventaRepository.findVentasHoy().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<VentaDTO> listarVentasPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        return ventaRepository.findByFechaHoraBetween(inicio, fin).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public VentaDTO crearVenta(VentaDTO dto) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new BadRequestException("La venta debe tener al menos un producto");
        }

        Venta venta = Venta.builder()
                .fechaHora(LocalDateTime.now())
                .estado(Venta.EstadoVenta.COMPLETADA)
                .observaciones(dto.getObservaciones())
                .total(BigDecimal.ZERO)
                .detalles(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaDTO detalleDTO : dto.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoCodigoBarras())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "código", detalleDTO.getProductoCodigoBarras()));

            // Verificar stock
            if (producto.getStockActual() < detalleDTO.getCantidad()) {
                throw new StockInsuficienteException(producto.getNombre(),
                        producto.getStockActual(), detalleDTO.getCantidad());
            }

            // Crear detalle
            BigDecimal precioUnitario = producto.getPrecioVenta();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(detalleDTO.getCantidad()));

            DetalleVenta detalle = DetalleVenta.builder()
                    .producto(producto)
                    .cantidad(detalleDTO.getCantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotal)
                    .build();

            venta.agregarDetalle(detalle);
            total = total.add(subtotal);

            // Descontar stock
            int stockAnterior = producto.getStockActual();
            producto.setStockActual(stockAnterior - detalleDTO.getCantidad());
            producto.setFechaUltimaVenta(LocalDateTime.now());
            productoRepository.save(producto);

            // Registrar movimiento
            MovimientoInventario movimiento = MovimientoInventario.builder()
                    .producto(producto)
                    .tipoMovimiento(MovimientoInventario.TipoMovimiento.SALIDA)
                    .cantidad(detalleDTO.getCantidad())
                    .fechaHora(LocalDateTime.now())
                    .motivo("Venta")
                    .stockAnterior(stockAnterior)
                    .stockNuevo(producto.getStockActual())
                    .build();
            movimientoRepository.save(movimiento);
        }

        venta.setTotal(total);
        venta = ventaRepository.save(venta);

        // Actualizar referencia de movimientos
        for (DetalleVenta detalle : venta.getDetalles()) {
            MovimientoInventario mov = movimientoRepository
                    .findByProductoCodigoBarrasOrderByFechaHoraDesc(detalle.getProducto().getCodigoBarras())
                    .stream().findFirst().orElse(null);
            if (mov != null) {
                mov.setReferencia("VENTA-" + venta.getId());
                movimientoRepository.save(mov);
            }
        }

        return convertirADTO(venta);
    }

    public VentaDTO cancelarVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", "id", id));

        if (venta.getEstado() == Venta.EstadoVenta.CANCELADA) {
            throw new BadRequestException("La venta ya está cancelada");
        }

        // Devolver stock
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            int stockAnterior = producto.getStockActual();
            producto.setStockActual(stockAnterior + detalle.getCantidad());
            productoRepository.save(producto);

            // Registrar movimiento de devolución
            MovimientoInventario movimiento = MovimientoInventario.builder()
                    .producto(producto)
                    .tipoMovimiento(MovimientoInventario.TipoMovimiento.DEVOLUCION)
                    .cantidad(detalle.getCantidad())
                    .fechaHora(LocalDateTime.now())
                    .motivo("Cancelación de venta #" + id)
                    .stockAnterior(stockAnterior)
                    .stockNuevo(producto.getStockActual())
                    .referencia("CANCEL-VENTA-" + id)
                    .build();
            movimientoRepository.save(movimiento);
        }

        venta.setEstado(Venta.EstadoVenta.CANCELADA);
        venta = ventaRepository.save(venta);

        return convertirADTO(venta);
    }

    public BigDecimal obtenerTotalVentasHoy() {
        BigDecimal total = ventaRepository.sumTotalVentasHoy();
        return total != null ? total : BigDecimal.ZERO;
    }

    public Long contarVentasHoy() {
        return ventaRepository.countVentasHoy();
    }

    public BigDecimal obtenerTotalVentasPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        BigDecimal total = ventaRepository.sumTotalVentasByFecha(inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<VentaDTO> obtenerVentasPorProducto(String codigoProducto) {
        return ventaRepository.findVentasByProducto(codigoProducto).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private VentaDTO convertirADTO(Venta venta) {
        List<DetalleVentaDTO> detalles = venta.getDetalles().stream()
                .map(d -> DetalleVentaDTO.builder()
                        .id(d.getId())
                        .productoCodigoBarras(d.getProducto().getCodigoBarras())
                        .productoNombre(d.getProducto().getNombre())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return VentaDTO.builder()
                .id(venta.getId())
                .fechaHora(venta.getFechaHora())
                .total(venta.getTotal())
                .estado(venta.getEstado().name())
                .observaciones(venta.getObservaciones())
                .detalles(detalles)
                .build();
    }
}

