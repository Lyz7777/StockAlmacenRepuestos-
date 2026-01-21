package com.inventario.repository;

import com.inventario.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaId(Long ventaId);

    List<DetalleVenta> findByProductoCodigoBarras(String codigoBarras);

    // Productos más vendidos
    @Query("SELECT d.producto.codigoBarras, d.producto.nombre, SUM(d.cantidad) as totalVendido " +
           "FROM DetalleVenta d WHERE d.venta.estado = 'COMPLETADA' " +
           "GROUP BY d.producto.codigoBarras, d.producto.nombre " +
           "ORDER BY totalVendido DESC")
    List<Object[]> findProductosMasVendidos();

    // Productos más vendidos por período
    @Query("SELECT d.producto.codigoBarras, d.producto.nombre, SUM(d.cantidad) as totalVendido " +
           "FROM DetalleVenta d WHERE d.venta.estado = 'COMPLETADA' " +
           "AND d.venta.fechaHora BETWEEN :inicio AND :fin " +
           "GROUP BY d.producto.codigoBarras, d.producto.nombre " +
           "ORDER BY totalVendido DESC")
    List<Object[]> findProductosMasVendidosPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Total vendido de un producto
    @Query("SELECT COALESCE(SUM(d.cantidad), 0) FROM DetalleVenta d " +
           "WHERE d.producto.codigoBarras = :codigo AND d.venta.estado = 'COMPLETADA'")
    Integer sumCantidadVendidaByProducto(@Param("codigo") String codigo);

    // Ventas de un producto por período
    @Query("SELECT d FROM DetalleVenta d WHERE d.producto.codigoBarras = :codigo " +
           "AND d.venta.estado = 'COMPLETADA' AND d.venta.fechaHora BETWEEN :inicio AND :fin")
    List<DetalleVenta> findVentasProductoPorPeriodo(
        @Param("codigo") String codigo,
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );
}

