package com.inventario.repository;

import com.inventario.entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    // Movimientos por producto
    List<MovimientoInventario> findByProductoCodigoBarrasOrderByFechaHoraDesc(String codigoBarras);

    Page<MovimientoInventario> findByProductoCodigoBarrasOrderByFechaHoraDesc(String codigoBarras, Pageable pageable);

    // Movimientos por tipo
    List<MovimientoInventario> findByTipoMovimientoOrderByFechaHoraDesc(MovimientoInventario.TipoMovimiento tipo);

    // Movimientos por rango de fechas
    @Query("SELECT m FROM MovimientoInventario m WHERE m.fechaHora BETWEEN :inicio AND :fin ORDER BY m.fechaHora DESC")
    List<MovimientoInventario> findByFechaHoraBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Movimientos de un producto por rango de fechas
    @Query("SELECT m FROM MovimientoInventario m WHERE m.producto.codigoBarras = :codigo " +
           "AND m.fechaHora BETWEEN :inicio AND :fin ORDER BY m.fechaHora DESC")
    List<MovimientoInventario> findByProductoAndFecha(
        @Param("codigo") String codigo,
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );

    // Últimos movimientos
    @Query("SELECT m FROM MovimientoInventario m ORDER BY m.fechaHora DESC")
    List<MovimientoInventario> findUltimosMovimientos(Pageable pageable);

    // Contar entradas por producto
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoInventario m " +
           "WHERE m.producto.codigoBarras = :codigo AND m.tipoMovimiento = 'ENTRADA'")
    Integer sumEntradasByProducto(@Param("codigo") String codigo);

    // Contar salidas por producto
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoInventario m " +
           "WHERE m.producto.codigoBarras = :codigo AND m.tipoMovimiento = 'SALIDA'")
    Integer sumSalidasByProducto(@Param("codigo") String codigo);
}

