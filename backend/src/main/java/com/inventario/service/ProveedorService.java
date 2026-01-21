package com.inventario.service;

import com.inventario.dto.ProveedorDTO;
import com.inventario.entity.Proveedor;
import com.inventario.exception.DuplicateResourceException;
import com.inventario.exception.ResourceNotFoundException;
import com.inventario.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public List<ProveedorDTO> listarTodos() {
        return proveedorRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public ProveedorDTO obtenerPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
        return convertirADTO(proveedor);
    }

    public ProveedorDTO crear(ProveedorDTO dto) {
        if (dto.getRuc() != null && !dto.getRuc().isEmpty() &&
            proveedorRepository.existsByRuc(dto.getRuc())) {
            throw new DuplicateResourceException("Proveedor", "RUC", dto.getRuc());
        }

        Proveedor proveedor = Proveedor.builder()
                .nombre(dto.getNombre())
                .ruc(dto.getRuc())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .contactoPrincipal(dto.getContactoPrincipal())
                .productosSuministra(dto.getProductosSuministra())
                .activo(true)
                .build();

        proveedor = proveedorRepository.save(proveedor);
        return convertirADTO(proveedor);
    }

    public ProveedorDTO actualizar(Long id, ProveedorDTO dto) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));

        // Verificar RUC duplicado
        if (dto.getRuc() != null && !dto.getRuc().isEmpty()) {
            proveedorRepository.findByRuc(dto.getRuc())
                    .ifPresent(p -> {
                        if (!p.getId().equals(id)) {
                            throw new DuplicateResourceException("Proveedor", "RUC", dto.getRuc());
                        }
                    });
        }

        proveedor.setNombre(dto.getNombre());
        proveedor.setRuc(dto.getRuc());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        proveedor.setContactoPrincipal(dto.getContactoPrincipal());
        proveedor.setProductosSuministra(dto.getProductosSuministra());

        proveedor = proveedorRepository.save(proveedor);
        return convertirADTO(proveedor);
    }

    public void eliminar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));

        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }

    public List<ProveedorDTO> buscarPorNombre(String nombre) {
        return proveedorRepository.buscarPorNombre(nombre).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<ProveedorDTO> autocompletar(String texto) {
        return proveedorRepository.buscarPorNombreORuc(texto).stream()
                .limit(10)
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private ProveedorDTO convertirADTO(Proveedor proveedor) {
        return ProveedorDTO.builder()
                .id(proveedor.getId())
                .nombre(proveedor.getNombre())
                .ruc(proveedor.getRuc())
                .direccion(proveedor.getDireccion())
                .telefono(proveedor.getTelefono())
                .email(proveedor.getEmail())
                .contactoPrincipal(proveedor.getContactoPrincipal())
                .productosSuministra(proveedor.getProductosSuministra())
                .activo(proveedor.getActivo())
                .cantidadProductos((long) proveedor.getProductos().size())
                .build();
    }
}

