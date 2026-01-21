package com.inventario.repository;

import com.inventario.entity.OrdenCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    // Órdenes por estado
    List<OrdenCompra> findByEstadoOrderByFechaOrdenDesc(OrdenCompra.EstadoOrden estado);

    Page<OrdenCompra> findByEstadoOrderByFechaOrdenDesc(OrdenCompra.EstadoOrden estado, Pageable pageable);

    // Órdenes por proveedor
    List<OrdenCompra> findByProveedorIdOrderByFechaOrdenDesc(Long proveedorId);

    // Órdenes por rango de fechas
    @Query("SELECT o FROM OrdenCompra o WHERE o.fechaOrden BETWEEN :inicio AND :fin ORDER BY o.fechaOrden DESC")
    List<OrdenCompra> findByFechaOrdenBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Órdenes pendientes
    @Query("SELECT o FROM OrdenCompra o WHERE o.estado IN ('PENDIENTE', 'ENVIADA') ORDER BY o.fechaOrden DESC")
    List<OrdenCompra> findOrdenesPendientes();

    // Contar órdenes pendientes
    @Query("SELECT COUNT(o) FROM OrdenCompra o WHERE o.estado IN ('PENDIENTE', 'ENVIADA')")
    Long countOrdenesPendientes();
}

