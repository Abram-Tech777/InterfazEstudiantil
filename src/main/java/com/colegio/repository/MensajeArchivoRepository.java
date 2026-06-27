package com.colegio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.colegio.entity.MensajeArchivo;

public interface MensajeArchivoRepository extends JpaRepository<MensajeArchivo, Integer> {
    List<MensajeArchivo> findByMensaje_IdMensaje(Integer idMensaje);
}
