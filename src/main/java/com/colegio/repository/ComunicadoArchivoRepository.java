package com.colegio.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.colegio.entity.ComunicadoArchivo;

public interface ComunicadoArchivoRepository extends JpaRepository<ComunicadoArchivo, Integer> {
    List<ComunicadoArchivo> findByComunicado_IdComunicado(int idComunicado);
}
