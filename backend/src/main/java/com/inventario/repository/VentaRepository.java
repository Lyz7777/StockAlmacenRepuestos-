package com.inventario.repository;

import com.inventario.entity.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Ventas por estado
    List<Venta> findByEstado(Venta.EstadoVenta estado);

    // Ventas por rango de fechas
    @Query("SELECT v FROM Venta v WHERE v.fechaHora BETWEEN :inicio AND :fin ORDER BY v.fechaHora DESC")
    List<Venta> findByFechaHoraBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Ventas del día
    @Query("SELECT v FROM Venta v WHERE DATE(v.fechaHora) = CURRENT_DATE AND v.estado = 'COMPLETADA' ORDER BY v.fechaHora DESC")
    List<Venta> findVentasHoy();

    // Total ventas por rango de fechas
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = 'COMPLETADA' AND v.fechaHora BETWEEN :inicio AND :fin")
    BigDecimal sumTotalVentasByFecha(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Ventas paginadas
    Page<Venta> findByEstado(Venta.EstadoVenta estado, Pageable pageable);

    // Contar ventas completadas del día
    @Query("SELECT COUNT(v) FROM Venta v WHERE DATE(v.fechaHora) = CURRENT_DATE AND v.estado = 'COMPLETADA'")
    Long countVentasHoy();

    // Total de ventas del día
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE DATE(v.fechaHora) = CURRENT_DATE AND v.estado = 'COMPLETADA'")
    BigDecimal sumTotalVentasHoy();

    // Ventas por producto
    @Query("SELECT v FROM Venta v JOIN v.detalles d WHERE d.producto.codigoBarras = :codigoProducto ORDER BY v.fechaHora DESC")
    List<Venta> findVentasByProducto(@Param("codigoProducto") String codigoProducto);

    // Estadísticas de ventas mensuales
    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', v.fechaHora) as mes, SUM(v.total) as total " +
           "FROM Venta v WHERE v.estado = 'COMPLETADA' AND v.fechaHora >= :desde " +
           "GROUP BY FUNCTION('DATE_TRUNC', 'month', v.fechaHora) ORDER BY mes")
    List<Object[]> getVentasMensuales(@Param("desde") LocalDateTime desde);

    // Estadísticas de ventas diarias
    @Query("SELECT CAST(v.fechaHora AS date) as dia, SUM(v.total) as total " +
           "FROM Venta v WHERE v.estado = 'COMPLETADA' AND v.fechaHora BETWEEN :inicio AND :fin " +
           "GROUP BY CAST(v.fechaHora AS date) ORDER BY dia")
    List<Object[]> getVentasDiarias(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}

