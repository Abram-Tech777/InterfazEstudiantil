package com.colegio.service.impl;


import java.util.List;
import java.util.Optional;

import com.colegio.entity.Docente;

public interface DocenteService {
    Docente crearDocente(Docente docente);
    Docente getDocenteById(int id);
    List<Docente> listarTodos();
    Docente actualizarDocente(int id, Docente docente);
    void eliminarDocente(int id);
    Optional<Docente> findByUsuarioId(int idUsuario);
}