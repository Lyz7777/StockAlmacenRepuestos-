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
@Table(name = "ordenes_compra", indexes = {
    @Index(name = "idx_orden_fecha", columnList = "fechaOrden"),
    @Index(name = "idx_orden_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaOrden = LocalDateTime.now();

    @Column
    private LocalDate fechaEntregaEstimada;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    @DecimalMin(value = "0.0", inclusive = true, message = "El total debe ser mayor o igual a 0")
    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleOrdenCompra> detalles = new ArrayList<>();

    // Método para agregar detalle
    public void agregarDetalle(DetalleOrdenCompra detalle) {
        detalles.add(detalle);
        detalle.setOrdenCompra(this);
    }

    // Método para remover detalle
    public void removerDetalle(DetalleOrdenCompra detalle) {
        detalles.remove(detalle);
        detalle.setOrdenCompra(null);
    }

    // Método para calcular el total
    public void calcularTotal() {
        this.total = detalles.stream()
            .map(d -> d.getPrecioCompra().multiply(BigDecimal.valueOf(d.getCantidadSolicitada())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public enum EstadoOrden {
        PENDIENTE,
        ENVIADA,
        RECIBIDA_PARCIAL,
        RECIBIDA,
        CANCELADA
    }
}

