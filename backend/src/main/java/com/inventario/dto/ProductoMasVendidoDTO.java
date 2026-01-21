package com.inventario.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoMasVendidoDTO {

    private String codigoBarras;
    private String nombre;
    private Long cantidadVendida;
    private Integer stockActual;
}

