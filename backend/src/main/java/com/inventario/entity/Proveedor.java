package com.inventario.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String nombre;

    @Size(max = 20, message = "El RUC/NIT no puede exceder 20 caracteres")
    @Column(length = 20)
    private String ruc;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    @Column(length = 500)
    private String direccion;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(length = 20)
    private String telefono;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(length = 100)
    private String email;

    @Size(max = 100, message = "El contacto no puede exceder 100 caracteres")
    @Column(length = 100)
    private String contactoPrincipal;

    @Size(max = 1000, message = "Los productos que suministra no puede exceder 1000 caracteres")
    @Column(length = 1000)
    private String productosSuministra;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @OneToMany(mappedBy = "proveedor", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();

    @OneToMany(mappedBy = "proveedor", fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrdenCompra> ordenesCompra = new ArrayList<>();
}

