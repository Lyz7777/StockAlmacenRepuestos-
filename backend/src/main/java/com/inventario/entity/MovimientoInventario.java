package com.inventario.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario", indexes = {
    @Index(name = "idx_movimiento_fecha", columnList = "fechaHora"),
    @Index(name = "idx_movimiento_tipo", columnList = "tipoMovimiento")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_codigo", nullable = false)
    private Producto producto;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    @Column(length = 500)
    private String motivo;

    @Column
    private Integer stockAnterior;

    @Column
    private Integer stockNuevo;

    @Size(max = 100, message = "La referencia no puede exceder 100 caracteres")
    @Column(length = 100)
    private String referencia; // Ej: número de venta, número de orden de compra

    public enum TipoMovimiento {
        ENTRADA,      // Ingreso de mercancía
        SALIDA,       // Venta
        AJUSTE_POSITIVO,  // Ajuste manual que aumenta stock
        AJUSTE_NEGATIVO,  // Ajuste manual que disminuye stock
        DEVOLUCION    // Devolución de producto
    }
}

