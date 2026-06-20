package com.colegio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.colegio.repository.ComunicadoRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ComunicadoCleanupService {

    @Autowired
    private ComunicadoRepository comunicadoRepository;

    /**
     * Limpia comunicados cada bimestre (cada 40 días aproximadamente).
     * Se ejecuta cada día a las 3 AM para verificar si es necesario limpiar.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarComunicadosAntiguos() {
        try {
            // Obtén comunicados más antiguos a 120 días (3 bimestres * 40 días aprox)
            LocalDateTime fechaLimite = LocalDateTime.now().minus(120, ChronoUnit.DAYS);
            
            // Elimina comunicados anteriores a esa fecha
            long deleted = comunicadoRepository.deleteOlderThan(fechaLimite);
            
            if (deleted > 0) {
                System.out.println("✓ Se eliminaron " + deleted + " comunicados antiguos (más de 120 días)");
            }
        } catch (Exception e) {
            System.err.println("✗ Error al limpiar comunicados: " + e.getMessage());
        }
    }
}
