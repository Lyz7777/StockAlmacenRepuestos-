package com.inventario.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteInventarioDTO {

    private Long totalProductos;
    private Long productosConStock;
    private Long productosAgotados;
    private Long productosStockBajo;
    private BigDecimal valorTotalInventario;
    private List<InventarioPorCategoriaDTO> inventarioPorCategoria;
}

