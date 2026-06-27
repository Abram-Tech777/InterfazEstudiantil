package com.colegio.dto;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class FilaHorarioDTO {

    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean recreo;
    private Map<String, HorarioDTO> horariosPorDia;

    public FilaHorarioDTO() {
        this.horariosPorDia = new HashMap<>();
    }

    public FilaHorarioDTO(LocalTime horaInicio, LocalTime horaFin, boolean recreo) {
        this();
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.recreo = recreo;
    }

    public void asignarHorario(String dia, HorarioDTO horario) {
        horariosPorDia.put(dia, horario);
    }

    public HorarioDTO getHorario(String dia) {
        return horariosPorDia.get(dia);
    }

    public boolean tieneHorario(String dia) {
        return horariosPorDia.containsKey(dia) && horariosPorDia.get(dia) != null;
    }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public boolean isRecreo() { return recreo; }
    public void setRecreo(boolean recreo) { this.recreo = recreo; }

    public Map<String, HorarioDTO> getHorariosPorDia() { return horariosPorDia; }
    public void setHorariosPorDia(Map<String, HorarioDTO> horariosPorDia) { this.horariosPorDia = horariosPorDia; }
}
