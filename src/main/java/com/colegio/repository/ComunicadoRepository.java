package com.colegio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.colegio.entity.Comunicado;

public interface ComunicadoRepository extends JpaRepository<Comunicado, Integer> {

    @Query("SELECT c FROM Comunicado c WHERE c.aula.idAula = :idAula ORDER BY c.fechaEmision DESC")
    List<Comunicado> listarPorAula(@Param("idAula") int idAula);

    @Query("SELECT DISTINCT c FROM Comunicado c WHERE "
         + "(c.aula IS NOT NULL AND c.aula.idAula = :idAula) "
         + "OR (c.grado IS NOT NULL AND c.grado = :grado) "
         + "OR (c.aula IS NULL AND c.grado IS NULL) "
         + "ORDER BY c.fechaEmision DESC")
    List<Comunicado> listarParaAulaOGrado(@Param("idAula") int idAula, @Param("grado") String grado);

    @Query("SELECT c FROM Comunicado c ORDER BY c.fechaEmision DESC")
    List<Comunicado> listarTodosOrdenados();

    @Query("SELECT c FROM Comunicado c WHERE c.autor.idUsuario = :idUsuario ORDER BY c.fechaEmision DESC")
    List<Comunicado> listarPorAutor(@Param("idUsuario") int idUsuario);
}
