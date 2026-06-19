package com.colegio.service.impl;

import java.util.List;
import java.util.Optional;

import com.colegio.entity.Aula;

public interface AulaService {
	Aula guardarAula(Aula aula);
   
	Aula obtenerAulaPorId(int id);
   
	Optional<Aula> buscarPorGradoYSeccion(String grado, String seccion);
   
	List<Aula> listarTodas();
   
    Aula actualizarAula(int id, Aula aula);
    void eliminarAula(int id);
}
