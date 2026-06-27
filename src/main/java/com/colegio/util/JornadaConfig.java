package com.colegio.util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.colegio.dto.SlotDTO;

@Component
@ConfigurationProperties(prefix = "colegio.jornada")
public class JornadaConfig {

    private LocalTime inicio = LocalTime.of(7, 30);
    private LocalTime fin = LocalTime.of(15, 30);
    private LocalTime recreoInicio = LocalTime.of(10, 0);
    private int recreoDuracion = 45;
    private int claseDuracion = 45;

    public List<SlotDTO> generarSlots() {
        List<SlotDTO> slots = new ArrayList<>();
        LocalTime current = inicio;
        int numero = 1;

        while (current.isBefore(fin)) {
            LocalTime slotFin = current.plusMinutes(claseDuracion);
            if (slotFin.isAfter(fin)) {
                slotFin = fin;
            }

            String tipo;
            LocalTime recreoFin = recreoInicio.plusMinutes(recreoDuracion);

            if (!current.isBefore(recreoInicio) && !current.isAfter(recreoFin.minusMinutes(1))) {
                tipo = "RECREO";
                slotFin = recreoFin;
            } else if (!current.isBefore(recreoInicio) && current.isBefore(recreoFin)) {
                tipo = "TRANSICION";
            } else {
                tipo = "CLASE";
            }

            if (current.isBefore(fin) && current.plusMinutes(1).isBefore(fin)) {
                slots.add(new SlotDTO(current, slotFin, tipo, numero));
                numero++;
            }

            if ("RECREO".equals(tipo)) {
                current = recreoFin;
            } else {
                current = current.plusMinutes(claseDuracion);
            }
        }

        return slots;
    }

    public LocalTime getInicio() { return inicio; }
    public void setInicio(LocalTime inicio) { this.inicio = inicio; }

    public LocalTime getFin() { return fin; }
    public void setFin(LocalTime fin) { this.fin = fin; }

    public LocalTime getRecreoInicio() { return recreoInicio; }
    public void setRecreoInicio(LocalTime recreoInicio) { this.recreoInicio = recreoInicio; }

    public int getRecreoDuracion() { return recreoDuracion; }
    public void setRecreoDuracion(int recreoDuracion) { this.recreoDuracion = recreoDuracion; }

    public int getClaseDuracion() { return claseDuracion; }
    public void setClaseDuracion(int claseDuracion) { this.claseDuracion = claseDuracion; }
}
