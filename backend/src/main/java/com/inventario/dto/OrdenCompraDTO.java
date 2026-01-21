package com.inventario.dto;

import com.inventario.entity.OrdenCompra;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraDTO {

    private Long id;

    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    private String proveedorNombre;

    private LocalDateTime fechaOrden;

    private LocalDate fechaEntregaEstimada;

    private OrdenCompra.EstadoOrden estado;

    private BigDecimal total;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    @NotEmpty(message = "La orden debe tener al menos un producto")
    @Builder.Default
    private List<DetalleOrdenCompraDTO> detalles = new ArrayList<>();
}

