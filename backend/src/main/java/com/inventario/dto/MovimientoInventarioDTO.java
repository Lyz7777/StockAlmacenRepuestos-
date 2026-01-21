package com.inventario.dto;

import com.inventario.entity.MovimientoInventario;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventarioDTO {

    private Long id;

    @NotBlank(message = "El código del producto es obligatorio")
    private String productoCodigoBarras;

    private String productoNombre;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovimientoInventario.TipoMovimiento tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    private LocalDateTime fechaHora;

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;

    private Integer stockAnterior;

    private Integer stockNuevo;

    private String referencia;
}

