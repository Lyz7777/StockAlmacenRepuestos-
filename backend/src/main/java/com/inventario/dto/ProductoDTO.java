package com.inventario.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    private String codigoBarras;

    @Size(max = 50, message = "El código interno no puede exceder 50 caracteres")
    private String codigoInterno;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
    private String marca;

    @Size(max = 200, message = "El modelo compatible no puede exceder 200 caracteres")
    private String modeloCompatible;

    private Long categoriaId;
    private String categoriaNombre;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precioVenta;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stockActual;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    private Long proveedorId;
    private String proveedorNombre;

    private LocalDate fechaIngreso;
    private LocalDateTime fechaUltimaVenta;

    @Size(max = 100, message = "La ubicación no puede exceder 100 caracteres")
    private String ubicacion;

    private String imagenUrl;

    private Boolean activo;

    // Campos calculados
    private Boolean stockBajo;
    private Boolean agotado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

