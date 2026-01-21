package com.inventario.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorDTO {

    private Long id;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 20, message = "El RUC/NIT no puede exceder 20 caracteres")
    private String ruc;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String direccion;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Size(max = 100, message = "El contacto no puede exceder 100 caracteres")
    private String contactoPrincipal;

    @Size(max = 1000, message = "Los productos que suministra no puede exceder 1000 caracteres")
    private String productosSuministra;

    private Boolean activo;

    private Long cantidadProductos;
}

