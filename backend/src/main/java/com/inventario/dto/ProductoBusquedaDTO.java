package com.inventario.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoBusquedaDTO {

    // Filtros de texto
    private String texto;           // Búsqueda general
    private String nombre;
    private String codigoBarras;
    private String codigoInterno;
    private String marca;
    private String modeloCompatible;

    // Filtros por relación
    private Long categoriaId;
    private Long proveedorId;

    // Filtros de stock
    private Integer stockMin;
    private Integer stockMax;
    private Boolean soloAgotados;
    private Boolean soloStockBajo;

    // Paginación
    private Integer page;
    private Integer size;

    // Ordenamiento
    private String sortBy;          // nombre, precioVenta, stockActual, fechaIngreso
    private String sortDirection;   // ASC, DESC

    public Integer getPage() {
        return page != null ? page : 0;
    }

    public Integer getSize() {
        return size != null ? size : 20;
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "nombre";
    }

    public String getSortDirection() {
        return sortDirection != null ? sortDirection : "ASC";
    }
}

