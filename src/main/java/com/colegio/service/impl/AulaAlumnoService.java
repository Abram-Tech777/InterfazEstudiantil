package com.colegio.service.impl;

import java.util.List;

public interface AulaAlumnoService {
    void asignarAlumnosAula(int idAula, List<Integer> idsAlumnos);
}
