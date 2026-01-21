package com.inventario.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDTO {

    private Long id;

    private LocalDateTime fechaHora;

    private BigDecimal total;

    private String estado;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    @NotEmpty(message = "La venta debe tener al menos un producto")
    @Builder.Default
    private List<DetalleVentaDTO> detalles = new ArrayList<>();
}

