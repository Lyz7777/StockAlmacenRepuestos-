package com.inventario.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDiariaDTO {

    private LocalDate fecha;
    private BigDecimal total;
    private Long cantidadVentas;
}

