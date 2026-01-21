package com.inventario.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_producto_nombre", columnList = "nombre"),
    @Index(name = "idx_producto_codigo_interno", columnList = "codigoInterno"),
    @Index(name = "idx_producto_marca", columnList = "marca"),
    @Index(name = "idx_producto_modelo", columnList = "modeloCompatible"),
    @Index(name = "idx_producto_stock", columnList = "stockActual")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @Column(name = "codigo_barras", length = 50)
    private String codigoBarras;

    @Size(max = 50, message = "El código interno no puede exceder 50 caracteres")
    @Column(length = 50)
    private String codigoInterno;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(length = 1000)
    private String descripcion;

    @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
    @Column(length = 100)
    private String marca;

    @Size(max = 200, message = "El modelo compatible no puede exceder 200 caracteres")
    @Column(length = 200)
    private String modeloCompatible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    @Builder.Default
    private Integer stockActual = 0;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(nullable = false)
    @Builder.Default
    private Integer stockMinimo = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Column(nullable = false)
    @Builder.Default
    private LocalDate fechaIngreso = LocalDate.now();

    @Column
    private LocalDateTime fechaUltimaVenta;

    @Size(max = 100, message = "La ubicación no puede exceder 100 caracteres")
    @Column(length = 100)
    private String ubicacion;

    @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
    @Column(length = 500)
    private String imagenUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MovimientoInventario> movimientos = new ArrayList<>();

    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    @Builder.Default
    private List<DetalleVenta> detallesVenta = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Método para verificar si el stock está bajo
    public boolean isStockBajo() {
        return this.stockActual <= this.stockMinimo;
    }

    // Método para verificar si está agotado
    public boolean isAgotado() {
        return this.stockActual == 0;
    }
}

