 package com.inventario.service;

import com.inventario.dto.DetalleOrdenCompraDTO;
import com.inventario.dto.OrdenCompraDTO;
import com.inventario.entity.*;
import com.inventario.exception.BadRequestException;
import com.inventario.exception.ResourceNotFoundException;
import com.inventario.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final DetalleOrdenCompraRepository detalleOrdenRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public Page<OrdenCompraDTO> listarTodas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaOrden"));
        return ordenCompraRepository.findAll(pageable).map(this::convertirADTO);
    }

    public OrdenCompraDTO obtenerPorId(Long id) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra", "id", id));
        return convertirADTO(orden);
    }

    public List<OrdenCompraDTO> listarPendientes() {
        return ordenCompraRepository.findOrdenesPendientes().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<OrdenCompraDTO> listarPorProveedor(Long proveedorId) {
        return ordenCompraRepository.findByProveedorIdOrderByFechaOrdenDesc(proveedorId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public OrdenCompraDTO crear(OrdenCompraDTO dto) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new BadRequestException("La orden debe tener al menos un producto");
        }

        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", dto.getProveedorId()));

        OrdenCompra orden = OrdenCompra.builder()
                .proveedor(proveedor)
                .fechaOrden(LocalDateTime.now())
                .fechaEntregaEstimada(dto.getFechaEntregaEstimada())
                .estado(OrdenCompra.EstadoOrden.PENDIENTE)
                .observaciones(dto.getObservaciones())
                .total(BigDecimal.ZERO)
                .detalles(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleOrdenCompraDTO detalleDTO : dto.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoCodigoBarras())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "código", detalleDTO.getProductoCodigoBarras()));

            DetalleOrdenCompra detalle = DetalleOrdenCompra.builder()
                    .producto(producto)
                    .cantidadSolicitada(detalleDTO.getCantidadSolicitada())
                    .cantidadRecibida(0)
                    .precioCompra(detalleDTO.getPrecioCompra())
                    .build();

            orden.agregarDetalle(detalle);
            total = total.add(detalleDTO.getPrecioCompra()
                    .multiply(BigDecimal.valueOf(detalleDTO.getCantidadSolicitada())));
        }

        orden.setTotal(total);
        orden = ordenCompraRepository.save(orden);

        return convertirADTO(orden);
    }

    public OrdenCompraDTO actualizarEstado(Long id, OrdenCompra.EstadoOrden nuevoEstado) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra", "id", id));

        orden.setEstado(nuevoEstado);
        orden = ordenCompraRepository.save(orden);

        return convertirADTO(orden);
    }

    public OrdenCompraDTO recibirOrden(Long id, List<DetalleOrdenCompraDTO> detallesRecibidos) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra", "id", id));

        if (orden.getEstado() == OrdenCompra.EstadoOrden.RECIBIDA ||
            orden.getEstado() == OrdenCompra.EstadoOrden.CANCELADA) {
            throw new BadRequestException("No se puede recibir una orden " + orden.getEstado().name().toLowerCase());
        }

        boolean todoRecibido = true;

        for (DetalleOrdenCompraDTO detalleDTO : detallesRecibidos) {
            DetalleOrdenCompra detalle = orden.getDetalles().stream()
                    .filter(d -> d.getProducto().getCodigoBarras().equals(detalleDTO.getProductoCodigoBarras()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Producto no encontrado en la orden"));

            int cantidadARecibir = detalleDTO.getCantidadRecibida();
            if (cantidadARecibir > 0) {
                detalle.setCantidadRecibida(detalle.getCantidadRecibida() + cantidadARecibir);

                // Actualizar stock del producto
                Producto producto = detalle.getProducto();
                int stockAnterior = producto.getStockActual();
                producto.setStockActual(stockAnterior + cantidadARecibir);
                productoRepository.save(producto);

                // Registrar movimiento
                MovimientoInventario movimiento = MovimientoInventario.builder()
                        .producto(producto)
                        .tipoMovimiento(MovimientoInventario.TipoMovimiento.ENTRADA)
                        .cantidad(cantidadARecibir)
                        .fechaHora(LocalDateTime.now())
                        .motivo("Recepción de orden de compra #" + id)
                        .stockAnterior(stockAnterior)
                        .stockNuevo(producto.getStockActual())
                        .referencia("OC-" + id)
                        .build();
                movimientoRepository.save(movimiento);
            }

            if (detalle.getCantidadRecibida() < detalle.getCantidadSolicitada()) {
                todoRecibido = false;
            }
        }

        orden.setEstado(todoRecibido ? OrdenCompra.EstadoOrden.RECIBIDA : OrdenCompra.EstadoOrden.RECIBIDA_PARCIAL);
        orden = ordenCompraRepository.save(orden);

        return convertirADTO(orden);
    }

    public OrdenCompraDTO cancelar(Long id) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra", "id", id));

        if (orden.getEstado() == OrdenCompra.EstadoOrden.RECIBIDA) {
            throw new BadRequestException("No se puede cancelar una orden ya recibida");
        }

        orden.setEstado(OrdenCompra.EstadoOrden.CANCELADA);
        orden = ordenCompraRepository.save(orden);

        return convertirADTO(orden);
    }

    // Generar orden sugerida basada en productos con stock bajo
    public OrdenCompraDTO generarOrdenSugerida(Long proveedorId) {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", proveedorId));

        List<Producto> productosStockBajo = productoRepository.findProductosStockBajo().stream()
                .filter(p -> p.getProveedor() != null && p.getProveedor().getId().equals(proveedorId))
                .collect(Collectors.toList());

        if (productosStockBajo.isEmpty()) {
            throw new BadRequestException("No hay productos con stock bajo para este proveedor");
        }

        List<DetalleOrdenCompraDTO> detalles = productosStockBajo.stream()
                .map(p -> DetalleOrdenCompraDTO.builder()
                        .productoCodigoBarras(p.getCodigoBarras())
                        .productoNombre(p.getNombre())
                        .cantidadSolicitada(p.getStockMinimo() * 2 - p.getStockActual()) // Sugerencia: completar al doble del mínimo
                        .stockActual(p.getStockActual())
                        .stockMinimo(p.getStockMinimo())
                        .precioCompra(p.getPrecioVenta().multiply(BigDecimal.valueOf(0.7))) // Estimación: 70% del precio de venta
                        .build())
                .collect(Collectors.toList());

        return OrdenCompraDTO.builder()
                .proveedorId(proveedorId)
                .proveedorNombre(proveedor.getNombre())
                .detalles(detalles)
                .build();
    }

    public Long contarPendientes() {
        return ordenCompraRepository.countOrdenesPendientes();
    }

    private OrdenCompraDTO convertirADTO(OrdenCompra orden) {
        List<DetalleOrdenCompraDTO> detalles = orden.getDetalles().stream()
                .map(d -> DetalleOrdenCompraDTO.builder()
                        .id(d.getId())
                        .productoCodigoBarras(d.getProducto().getCodigoBarras())
                        .productoNombre(d.getProducto().getNombre())
                        .cantidadSolicitada(d.getCantidadSolicitada())
                        .cantidadRecibida(d.getCantidadRecibida())
                        .precioCompra(d.getPrecioCompra())
                        .stockActual(d.getProducto().getStockActual())
                        .stockMinimo(d.getProducto().getStockMinimo())
                        .build())
                .collect(Collectors.toList());

        return OrdenCompraDTO.builder()
                .id(orden.getId())
                .proveedorId(orden.getProveedor().getId())
                .proveedorNombre(orden.getProveedor().getNombre())
                .fechaOrden(orden.getFechaOrden())
                .fechaEntregaEstimada(orden.getFechaEntregaEstimada())
                .estado(orden.getEstado())
                .total(orden.getTotal())
                .observaciones(orden.getObservaciones())
                .detalles(detalles)
                .build();
    }
}
package com.inventario.service;

import com.inventario.dto.CategoriaDTO;
import com.inventario.entity.Categoria;
import com.inventario.exception.DuplicateResourceException;
import com.inventario.exception.ResourceNotFoundException;
import com.inventario.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaDTO> listarTodas() {
        return categoriaRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));
        return convertirADTO(categoria);
    }

    public CategoriaDTO crear(CategoriaDTO dto) {
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new DuplicateResourceException("Categoría", "nombre", dto.getNombre());
        }

        Categoria categoria = Categoria.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();

        categoria = categoriaRepository.save(categoria);
        return convertirADTO(categoria);
    }

    public CategoriaDTO actualizar(Long id, CategoriaDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));

        // Verificar si ya existe otra categoría con el mismo nombre
        categoriaRepository.findByNombreIgnoreCase(dto.getNombre())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) {
                        throw new DuplicateResourceException("Categoría", "nombre", dto.getNombre());
                    }
                });

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());

        categoria = categoriaRepository.save(categoria);
        return convertirADTO(categoria);
    }

    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));

        // Eliminación lógica
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    public List<CategoriaDTO> buscarPorNombre(String nombre) {
        return categoriaRepository.buscarPorNombre(nombre).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private CategoriaDTO convertirADTO(Categoria categoria) {
        return CategoriaDTO.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .activo(categoria.getActivo())
                .cantidadProductos((long) categoria.getProductos().size())
                .build();
    }
}

