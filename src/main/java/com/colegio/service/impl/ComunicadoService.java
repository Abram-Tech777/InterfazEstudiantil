package com.colegio.service.impl;

import java.util.List;
import com.colegio.entity.Alumno;
import com.colegio.entity.Comunicado;



public interface ComunicadoService {
    Comunicado crearComunicado(Comunicado comunicado);
    List<Comunicado> listarPorAula(int idAula);
    List<Comunicado> listarParaAlumno(Alumno alumno);
    List<Comunicado> listarTodos();
    void eliminarComunicado(Integer idComunicado);
}
