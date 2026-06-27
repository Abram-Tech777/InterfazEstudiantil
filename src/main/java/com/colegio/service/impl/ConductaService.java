package com.colegio.service.impl;

import com.colegio.entity.Conducta;
import java.util.List;

public interface ConductaService {
    List<Conducta> listarPorAlumno(Integer idAlumno);
    List<Conducta> listarPorAlumnoYBimestre(Integer idAlumno, Integer bimestre);
    Conducta guardar(Conducta conducta);
    Conducta actualizar(Conducta conducta);
    void eliminar(Integer idConducta);
    Conducta obtenerPorId(Integer idConducta);
    long contarPorAlumnoYTipo(Integer idAlumno, String tipo);
    long contarPorAlumnoYTipoYBimestre(Integer idAlumno, String tipo, Integer bimestre);
    List<Conducta> listarUltimasPorDocente(Integer idDocente);
}
