package com.inventario.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleOrdenCompraDTO {

    private Long id;

    @NotBlank(message = "El código del producto es obligatorio")
    private String productoCodigoBarras;

    private String productoNombre;

    @NotNull(message = "La cantidad solicitada es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidadSolicitada;

    private Integer cantidadRecibida;

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precioCompra;

    private Integer stockActual;

    private Integer stockMinimo;
}

