package com.colegio.service.impl;

import java.util.List;
import java.util.Map;
import com.colegio.entity.AulaDocente;

public interface AulaDocenteService {
    AulaDocente crearAsignacion(AulaDocente aulaDocente);
    List<AulaDocente> listarPorAula(int idAula);
    List<AulaDocente> listarPorDocenteActivos(int idDocente);
    List<Integer> listarAulasDelDocente(int idDocente);
    void desactivarAsignacion(int idAulaDocente);
    void activarAsignacion(int idAulaDocente);
    boolean estaAsignado(int idDocente, int idAula);
    void asignarDocentesAulaMultiple(int idAula, List<Integer> idsDocentes, String rol);
    void asignarDocentesAulaMultipleConRolesUnTutor(int idAula, Map<Integer, String> rolesPorDocente);
}
