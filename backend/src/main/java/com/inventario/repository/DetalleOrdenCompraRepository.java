package com.inventario.repository;

import com.inventario.entity.DetalleOrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleOrdenCompraRepository extends JpaRepository<DetalleOrdenCompra, Long> {

    List<DetalleOrdenCompra> findByOrdenCompraId(Long ordenId);

    List<DetalleOrdenCompra> findByProductoCodigoBarras(String codigoBarras);
}
package com.inventario.repository;

import com.inventario.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByActivoTrue();

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    @Query("SELECT c FROM Categoria c WHERE c.activo = true AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Categoria> buscarPorNombre(@Param("nombre") String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}

