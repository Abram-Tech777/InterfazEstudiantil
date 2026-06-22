package com.colegio.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.colegio.repository.ComunicadoRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ComunicadoCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(ComunicadoCleanupService.class);

    @Autowired
    private ComunicadoRepository comunicadoRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarComunicadosAntiguos() {
        try {
            LocalDateTime fechaLimite = LocalDateTime.now().minus(120, ChronoUnit.DAYS);
            long deleted = comunicadoRepository.deleteOlderThan(fechaLimite);
            
            if (deleted > 0) {
                logger.info("Se eliminaron {} comunicados antiguos (más de 120 días)", deleted);
            }
        } catch (Exception e) {
            logger.error("Error al limpiar comunicados: {}", e.getMessage());
        }
    }
}
