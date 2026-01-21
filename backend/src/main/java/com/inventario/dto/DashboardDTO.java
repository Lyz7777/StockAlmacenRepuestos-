package com.inventario.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    // Estadísticas de productos
    private Long totalProductos;
    private Long productosStockBajo;
    private Long productosAgotados;

    // Estadísticas de ventas
    private Long ventasHoy;
    private BigDecimal totalVentasHoy;
    private BigDecimal totalVentasSemana;
    private BigDecimal totalVentasMes;

    // Valor del inventario
    private BigDecimal valorInventario;

    // Órdenes pendientes
    private Long ordenesPendientes;

    // Top productos más vendidos
    private java.util.List<ProductoMasVendidoDTO> productosMasVendidos;

    // Productos con stock bajo
    private java.util.List<ProductoDTO> productosStockCritico;
}

