package com.colegio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.colegio.entity.Aula;

public interface AulaRepository extends JpaRepository<Aula, Integer>{

  
    @Query("SELECT a FROM Aula a WHERE a.grado = :grado AND a.seccion = :seccion")
    Optional<Aula> buscarPorGradoYSeccion(String grado, String seccion);
    
    boolean existsByGradoAndSeccion(String grado, String seccion);

    @Query("SELECT DISTINCT a.grado FROM Aula a ORDER BY a.grado")
    java.util.List<String> listarGradosDistintos();
}
