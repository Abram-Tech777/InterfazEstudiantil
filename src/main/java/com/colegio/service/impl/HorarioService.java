package com.colegio.service.impl;

import java.util.List;


import com.colegio.entity.Horario;
import com.colegio.repository.HorarioRepository;

public interface HorarioService {
	Horario crearHorario(Horario horario);
    Horario getHorarioById(int id);
    List<Horario> findAll();
    Horario updateHorario(int id, Horario horario);
    void deleteHorario(int id);
    HorarioRepository findHorarioRepository();
}
