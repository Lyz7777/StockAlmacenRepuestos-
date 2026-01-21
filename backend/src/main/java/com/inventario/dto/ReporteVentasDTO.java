package com.inventario.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteVentasDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal totalVentas;
    private Long cantidadVentas;
    private Long cantidadProductosVendidos;
    private BigDecimal promedioVenta;
    private List<VentaDiariaDTO> ventasDiarias;
    private List<ProductoMasVendidoDTO> productosMasVendidos;
}

