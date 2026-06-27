package com.colegio.service.impl;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;

import com.colegio.entity.Alumno;
import com.colegio.entity.Comunicado;



public interface ComunicadoService {
    Comunicado crearComunicado(Comunicado comunicado);
    Comunicado actualizarComunicado(Comunicado comunicado);
    List<Comunicado> listarPorAula(int idAula);
    List<Comunicado> listarParaAlumno(Alumno alumno);
    List<Comunicado> listarTodos();
    void eliminarComunicado(Integer idComunicado);
    Comunicado obtenerComunicadoPorId(Integer id);

    String guardarArchivo(MultipartFile archivo) throws Exception;
    ResponseEntity<Resource> descargarArchivo(Integer idComunicado) throws Exception;
}
