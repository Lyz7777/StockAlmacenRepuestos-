package com.inventario.repository;

import com.inventario.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String>, JpaSpecificationExecutor<Producto> {

    // Búsquedas básicas
    List<Producto> findByActivoTrue();

    Page<Producto> findByActivoTrue(Pageable pageable);

    // Búsqueda por código interno
    List<Producto> findByCodigoInternoContainingIgnoreCaseAndActivoTrue(String codigoInterno);

    // Búsqueda por nombre (parcial)
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> buscarPorNombre(@Param("nombre") String nombre);

    // Búsqueda por marca
    List<Producto> findByMarcaContainingIgnoreCaseAndActivoTrue(String marca);

    // Búsqueda por modelo compatible
    List<Producto> findByModeloCompatibleContainingIgnoreCaseAndActivoTrue(String modelo);

    // Búsqueda por categoría
    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);

    // Búsqueda por proveedor
    List<Producto> findByProveedorIdAndActivoTrue(Long proveedorId);

    // Productos con stock bajo (stock actual <= stock mínimo)
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stockActual <= p.stockMinimo")
    List<Producto> findProductosStockBajo();

    // Productos agotados
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stockActual = 0")
    List<Producto> findProductosAgotados();

    // Productos por rango de stock
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stockActual >= :min AND p.stockActual <= :max")
    List<Producto> findByStockRange(@Param("min") Integer min, @Param("max") Integer max);

    // Productos con stock mayor a X
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stockActual > :cantidad")
    List<Producto> findByStockMayorA(@Param("cantidad") Integer cantidad);

    // Productos con stock menor a X
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stockActual < :cantidad")
    List<Producto> findByStockMenorA(@Param("cantidad") Integer cantidad);

    // Autocompletado de marcas
    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.activo = true AND p.marca IS NOT NULL AND LOWER(p.marca) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<String> findMarcasAutocomplete(@Param("texto") String texto);

    // Autocompletado de modelos
    @Query("SELECT DISTINCT p.modeloCompatible FROM Producto p WHERE p.activo = true AND p.modeloCompatible IS NOT NULL AND LOWER(p.modeloCompatible) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<String> findModelosAutocomplete(@Param("texto") String texto);

    // Búsqueda avanzada combinada
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:codigoInterno IS NULL OR LOWER(p.codigoInterno) LIKE LOWER(CONCAT('%', :codigoInterno, '%'))) AND " +
           "(:marca IS NULL OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) AND " +
           "(:modelo IS NULL OR LOWER(p.modeloCompatible) LIKE LOWER(CONCAT('%', :modelo, '%'))) AND " +
           "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
           "(:proveedorId IS NULL OR p.proveedor.id = :proveedorId) AND " +
           "(:stockMin IS NULL OR p.stockActual >= :stockMin) AND " +
           "(:stockMax IS NULL OR p.stockActual <= :stockMax)")
    Page<Producto> busquedaAvanzada(
        @Param("nombre") String nombre,
        @Param("codigoInterno") String codigoInterno,
        @Param("marca") String marca,
        @Param("modelo") String modelo,
        @Param("categoriaId") Long categoriaId,
        @Param("proveedorId") Long proveedorId,
        @Param("stockMin") Integer stockMin,
        @Param("stockMax") Integer stockMax,
        Pageable pageable
    );

    // Actualizar fecha de última venta
    @Modifying
    @Query("UPDATE Producto p SET p.fechaUltimaVenta = :fecha WHERE p.codigoBarras = :codigo")
    void actualizarFechaUltimaVenta(@Param("codigo") String codigo, @Param("fecha") LocalDateTime fecha);

    // Actualizar stock
    @Modifying
    @Query("UPDATE Producto p SET p.stockActual = p.stockActual + :cantidad WHERE p.codigoBarras = :codigo")
    void actualizarStock(@Param("codigo") String codigo, @Param("cantidad") Integer cantidad);

    // Contar productos con stock bajo
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true AND p.stockActual <= p.stockMinimo")
    Long countProductosStockBajo();

    // Contar productos agotados
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true AND p.stockActual = 0")
    Long countProductosAgotados();

    // Verificar si existe código de barras
    boolean existsByCodigoBarras(String codigoBarras);

    // Búsqueda de texto completo para autocompletado
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.codigoInterno) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.marca) LIKE LOWER(CONCAT('%', :texto, '%')))")
    List<Producto> busquedaGeneral(@Param("texto") String texto, Pageable pageable);
}

