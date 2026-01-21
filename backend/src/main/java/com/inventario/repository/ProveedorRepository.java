package com.inventario.repository;

import com.inventario.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findByActivoTrue();

    Optional<Proveedor> findByRuc(String ruc);

    @Query("SELECT p FROM Proveedor p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Proveedor> buscarPorNombre(@Param("nombre") String nombre);

    @Query("SELECT p FROM Proveedor p WHERE p.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.ruc) LIKE LOWER(CONCAT('%', :texto, '%')))")
    List<Proveedor> buscarPorNombreORuc(@Param("texto") String texto);

    boolean existsByRuc(String ruc);
}

