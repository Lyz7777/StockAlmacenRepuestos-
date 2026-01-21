package com.inventario.service;

import com.inventario.dto.ProductoBusquedaDTO;
import com.inventario.dto.ProductoDTO;
import com.inventario.entity.Categoria;
import com.inventario.entity.MovimientoInventario;
import com.inventario.entity.Producto;
import com.inventario.entity.Proveedor;
import com.inventario.exception.DuplicateResourceException;
import com.inventario.exception.ResourceNotFoundException;
import com.inventario.repository.CategoriaRepository;
import com.inventario.repository.MovimientoInventarioRepository;
import com.inventario.repository.ProductoRepository;
import com.inventario.repository.ProveedorRepository;
import com.inventario.util.BarcodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final BarcodeGenerator barcodeGenerator;

    public Page<ProductoDTO> listarTodos(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return productoRepository.findByActivoTrue(pageable).map(this::convertirADTO);
    }

    public ProductoDTO obtenerPorCodigoBarras(String codigoBarras) {
        Producto producto = productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "código de barras", codigoBarras));
        return convertirADTO(producto);
    }

    public ProductoDTO crear(ProductoDTO dto) {
        // Si no tiene código de barras, generar uno automáticamente
        String codigoBarras = dto.getCodigoBarras();
        if (codigoBarras == null || codigoBarras.isEmpty()) {
            do {
                codigoBarras = barcodeGenerator.generarCodigoBarras();
            } while (productoRepository.existsByCodigoBarras(codigoBarras));
        } else if (productoRepository.existsByCodigoBarras(codigoBarras)) {
            throw new DuplicateResourceException("Producto", "código de barras", codigoBarras);
        }

        Producto producto = Producto.builder()
                .codigoBarras(codigoBarras)
                .codigoInterno(dto.getCodigoInterno() != null ? dto.getCodigoInterno() :
                        barcodeGenerator.generarCodigoInterno("PRD"))
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .marca(dto.getMarca())
                .modeloCompatible(dto.getModeloCompatible())
                .precioVenta(dto.getPrecioVenta())
                .stockActual(dto.getStockActual() != null ? dto.getStockActual() : 0)
                .stockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5)
                .fechaIngreso(LocalDate.now())
                .ubicacion(dto.getUbicacion())
                .imagenUrl(dto.getImagenUrl())
                .activo(true)
                .build();

        // Asignar categoría
        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", dto.getCategoriaId()));
            producto.setCategoria(categoria);
        }

        // Asignar proveedor
        if (dto.getProveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", dto.getProveedorId()));
            producto.setProveedor(proveedor);
        }

        producto = productoRepository.save(producto);

        // Registrar movimiento de entrada inicial si hay stock
        if (producto.getStockActual() > 0) {
            registrarMovimiento(producto, MovimientoInventario.TipoMovimiento.ENTRADA,
                    producto.getStockActual(), "Stock inicial al crear producto", null);
        }

        return convertirADTO(producto);
    }

    public ProductoDTO actualizar(String codigoBarras, ProductoDTO dto) {
        Producto producto = productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "código de barras", codigoBarras));

        producto.setCodigoInterno(dto.getCodigoInterno());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setMarca(dto.getMarca());
        producto.setModeloCompatible(dto.getModeloCompatible());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setStockMinimo(dto.getStockMinimo());
        producto.setUbicacion(dto.getUbicacion());
        producto.setImagenUrl(dto.getImagenUrl());

        // Actualizar categoría
        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", dto.getCategoriaId()));
            producto.setCategoria(categoria);
        } else {
            producto.setCategoria(null);
        }

        // Actualizar proveedor
        if (dto.getProveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", dto.getProveedorId()));
            producto.setProveedor(proveedor);
        } else {
            producto.setProveedor(null);
        }

        producto = productoRepository.save(producto);
        return convertirADTO(producto);
    }

    public void eliminar(String codigoBarras) {
        Producto producto = productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "código de barras", codigoBarras));

        producto.setActivo(false);
        productoRepository.save(producto);
    }

    public ProductoDTO ajustarStock(String codigoBarras, Integer cantidad, String motivo, boolean esEntrada) {
        Producto producto = productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "código de barras", codigoBarras));

        int stockAnterior = producto.getStockActual();
        int nuevoStock;
        MovimientoInventario.TipoMovimiento tipo;

        if (esEntrada) {
            nuevoStock = stockAnterior + cantidad;
            tipo = MovimientoInventario.TipoMovimiento.ENTRADA;
        } else {
            nuevoStock = Math.max(0, stockAnterior - cantidad);
            tipo = MovimientoInventario.TipoMovimiento.AJUSTE_NEGATIVO;
        }

        producto.setStockActual(nuevoStock);
        producto = productoRepository.save(producto);

        registrarMovimiento(producto, tipo, cantidad, motivo, null);

        return convertirADTO(producto);
    }

    // Búsqueda avanzada
    public Page<ProductoDTO> busquedaAvanzada(ProductoBusquedaDTO filtros) {
        Sort.Direction direction = Sort.Direction.fromString(filtros.getSortDirection());
        Pageable pageable = PageRequest.of(filtros.getPage(), filtros.getSize(),
                Sort.by(direction, filtros.getSortBy()));

        Integer stockMin = filtros.getStockMin();
        Integer stockMax = filtros.getStockMax();

        // Si busca agotados
        if (Boolean.TRUE.equals(filtros.getSoloAgotados())) {
            stockMin = 0;
            stockMax = 0;
        }

        return productoRepository.busquedaAvanzada(
                filtros.getNombre(),
                filtros.getCodigoInterno(),
                filtros.getMarca(),
                filtros.getModeloCompatible(),
                filtros.getCategoriaId(),
                filtros.getProveedorId(),
                stockMin,
                stockMax,
                pageable
        ).map(this::convertirADTO);
    }

    // Búsqueda general para autocompletado
    public List<ProductoDTO> busquedaGeneral(String texto) {
        Pageable pageable = PageRequest.of(0, 10);
        return productoRepository.busquedaGeneral(texto, pageable).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Productos con stock bajo
    public List<ProductoDTO> obtenerProductosStockBajo() {
        return productoRepository.findProductosStockBajo().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Productos agotados
    public List<ProductoDTO> obtenerProductosAgotados() {
        return productoRepository.findProductosAgotados().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Autocompletado de marcas
    public List<String> autocompletarMarcas(String texto) {
        return productoRepository.findMarcasAutocomplete(texto).stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    // Autocompletado de modelos
    public List<String> autocompletarModelos(String texto) {
        return productoRepository.findModelosAutocomplete(texto).stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    // Generar código de barras
    public String generarCodigoBarras() {
        String codigo;
        do {
            codigo = barcodeGenerator.generarCodigoBarras();
        } while (productoRepository.existsByCodigoBarras(codigo));
        return codigo;
    }

    // Generar imagen de código de barras
    public String generarImagenCodigoBarras(String codigo) {
        return barcodeGenerator.generarImagenCodigoBarras(codigo);
    }

    // Contadores
    public Long contarProductosStockBajo() {
        return productoRepository.countProductosStockBajo();
    }

    public Long contarProductosAgotados() {
        return productoRepository.countProductosAgotados();
    }

    // Método auxiliar para registrar movimientos
    private void registrarMovimiento(Producto producto, MovimientoInventario.TipoMovimiento tipo,
                                     Integer cantidad, String motivo, String referencia) {
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .tipoMovimiento(tipo)
                .cantidad(cantidad)
                .fechaHora(LocalDateTime.now())
                .motivo(motivo)
                .stockAnterior(producto.getStockActual() - (tipo == MovimientoInventario.TipoMovimiento.ENTRADA ? cantidad : -cantidad))
                .stockNuevo(producto.getStockActual())
                .referencia(referencia)
                .build();

        movimientoRepository.save(movimiento);
    }

    private ProductoDTO convertirADTO(Producto producto) {
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
                .fechaCreacion(producto.getFechaCreacion())
                .fechaActualizacion(producto.getFechaActualizacion())
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

