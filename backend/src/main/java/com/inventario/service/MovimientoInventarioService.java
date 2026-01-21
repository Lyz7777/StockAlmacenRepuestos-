package com.inventario.service;

import com.inventario.dto.MovimientoInventarioDTO;
import com.inventario.entity.MovimientoInventario;
import com.inventario.entity.Producto;
import com.inventario.exception.ResourceNotFoundException;
import com.inventario.repository.MovimientoInventarioRepository;
import com.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public Page<MovimientoInventarioDTO> listarTodos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaHora"));
        return movimientoRepository.findAll(pageable).map(this::convertirADTO);
    }

    public List<MovimientoInventarioDTO> listarPorProducto(String codigoBarras) {
        return movimientoRepository.findByProductoCodigoBarrasOrderByFechaHoraDesc(codigoBarras)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Page<MovimientoInventarioDTO> listarPorProductoPaginado(String codigoBarras, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaHora"));
        return movimientoRepository.findByProductoCodigoBarrasOrderByFechaHoraDesc(codigoBarras, pageable)
                .map(this::convertirADTO);
    }

    public List<MovimientoInventarioDTO> listarPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        return movimientoRepository.findByFechaHoraBetween(inicio, fin)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<MovimientoInventarioDTO> listarPorTipo(MovimientoInventario.TipoMovimiento tipo) {
        return movimientoRepository.findByTipoMovimientoOrderByFechaHoraDesc(tipo)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public MovimientoInventarioDTO registrarEntrada(String codigoBarras, Integer cantidad, String motivo) {
        Producto producto = productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "código", codigoBarras));

        int stockAnterior = producto.getStockActual();
        producto.setStockActual(stockAnterior + cantidad);
        productoRepository.save(producto);

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .tipoMovimiento(MovimientoInventario.TipoMovimiento.ENTRADA)
                .cantidad(cantidad)
                .fechaHora(LocalDateTime.now())
                .motivo(motivo)
                .stockAnterior(stockAnterior)
                .stockNuevo(producto.getStockActual())
                .build();

        movimiento = movimientoRepository.save(movimiento);
        return convertirADTO(movimiento);
    }

    public MovimientoInventarioDTO registrarAjuste(String codigoBarras, Integer cantidad,
                                                    String motivo, boolean esPositivo) {
        Producto producto = productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "código", codigoBarras));

        int stockAnterior = producto.getStockActual();
        int nuevoStock = esPositivo ? stockAnterior + cantidad : Math.max(0, stockAnterior - cantidad);
        producto.setStockActual(nuevoStock);
        productoRepository.save(producto);

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .tipoMovimiento(esPositivo ?
                        MovimientoInventario.TipoMovimiento.AJUSTE_POSITIVO :
                        MovimientoInventario.TipoMovimiento.AJUSTE_NEGATIVO)
                .cantidad(cantidad)
                .fechaHora(LocalDateTime.now())
                .motivo(motivo)
                .stockAnterior(stockAnterior)
                .stockNuevo(producto.getStockActual())
                .build();

        movimiento = movimientoRepository.save(movimiento);
        return convertirADTO(movimiento);
    }

    public List<MovimientoInventarioDTO> obtenerUltimosMovimientos(int cantidad) {
        Pageable pageable = PageRequest.of(0, cantidad);
        return movimientoRepository.findUltimosMovimientos(pageable)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private MovimientoInventarioDTO convertirADTO(MovimientoInventario movimiento) {
        return MovimientoInventarioDTO.builder()
                .id(movimiento.getId())
                .productoCodigoBarras(movimiento.getProducto().getCodigoBarras())
                .productoNombre(movimiento.getProducto().getNombre())
                .tipoMovimiento(movimiento.getTipoMovimiento())
                .cantidad(movimiento.getCantidad())
                .fechaHora(movimiento.getFechaHora())
                .motivo(movimiento.getMotivo())
                .stockAnterior(movimiento.getStockAnterior())
                .stockNuevo(movimiento.getStockNuevo())
                .referencia(movimiento.getReferencia())
                .build();
    }
}

