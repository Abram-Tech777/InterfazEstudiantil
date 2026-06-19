package com.colegio.service.impl;

import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.colegio.entity.AulaDocente;
import com.colegio.entity.Aula;
import com.colegio.entity.Docente;
import com.colegio.repository.AulaDocenteRepository;
import com.colegio.repository.AulaRepository;
import com.colegio.repository.DocenteRepository;

@Service
@Transactional
public class AulaDocenteServiceImpl implements AulaDocenteService {

    private final AulaDocenteRepository aulaDocenteRepository;
    private final DocenteRepository docenteRepository;
    private final AulaRepository aulaRepository;

    public AulaDocenteServiceImpl(AulaDocenteRepository aulaDocenteRepository,
                                  DocenteRepository docenteRepository,
                                  AulaRepository aulaRepository) {
        this.aulaDocenteRepository = aulaDocenteRepository;
        this.docenteRepository = docenteRepository;
        this.aulaRepository = aulaRepository;
    }

    @Override
    public AulaDocente crearAsignacion(AulaDocente aulaDocente) {
        if (aulaDocente.getDocente() == null || aulaDocente.getAula() == null) {
            throw new IllegalArgumentException("Docente y Aula son obligatorios");
        }
        return aulaDocenteRepository.save(aulaDocente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AulaDocente> listarPorAula(int idAula) {
        return aulaDocenteRepository.findByAula_IdAula(idAula);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AulaDocente> listarPorDocenteActivos(int idDocente) {
        return aulaDocenteRepository.findByDocente_IdDocenteAndActivoTrue(idDocente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> listarAulasDelDocente(int idDocente) {
        return aulaDocenteRepository.findAulaIdsByDocenteId(idDocente);
    }

    @Override
    public void desactivarAsignacion(int idAulaDocente) {
        AulaDocente ad = aulaDocenteRepository.findById(idAulaDocente)
                .orElseThrow(() -> new EntityNotFoundException("Asignación no encontrada"));
        ad.setActivo(false);
        aulaDocenteRepository.save(ad);
    }

    @Override
    public void activarAsignacion(int idAulaDocente) {
        AulaDocente ad = aulaDocenteRepository.findById(idAulaDocente)
                .orElseThrow(() -> new EntityNotFoundException("Asignación no encontrada"));
        ad.setActivo(true);
        aulaDocenteRepository.save(ad);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaAsignado(int idDocente, int idAula) {
        return aulaDocenteRepository.findByDocente_IdDocenteAndAula_IdAula(idDocente, idAula)
                .map(AulaDocente::isActivo)
                .orElse(false);
    }

    @Override
    public void asignarDocentesAulaMultiple(int idAula, List<Integer> idsDocentes, String rol) {
        Aula aula = aulaRepository.findById(idAula)
                .orElseThrow(() -> new EntityNotFoundException("Aula no encontrada"));


        List<AulaDocente> actuales = aulaDocenteRepository.findByAula_IdAula(idAula);


        for (AulaDocente ad : actuales) {
            if (!idsDocentes.contains(ad.getDocente().getIdDocente())) {
                ad.setActivo(false);
                aulaDocenteRepository.save(ad);
            } else {

                if (!ad.isActivo()) {
                    ad.setActivo(true);
                    aulaDocenteRepository.save(ad);
                }
            }
        }


        for (Integer idDocente : idsDocentes) {
            Docente docente = docenteRepository.findById(idDocente)
                    .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado"));

  
            var existente = aulaDocenteRepository.findByDocente_IdDocenteAndAula_IdAula(idDocente, idAula);
            if (existente.isPresent()) {
                AulaDocente ad = existente.get();
                ad.setActivo(true);
                ad.setRol(rol);
                aulaDocenteRepository.save(ad);
            } else {
                AulaDocente nuevaAsignacion = new AulaDocente(docente, aula, rol);
                aulaDocenteRepository.save(nuevaAsignacion);
            }
        }
    }

    @Override
    public void asignarDocentesAulaMultipleConRolesUnTutor(int idAula, Map<Integer, String> rolesPorDocente) {
        Aula aula = aulaRepository.findById(idAula)
                .orElseThrow(() -> new EntityNotFoundException("Aula no encontrada"));


        List<AulaDocente> actuales = aulaDocenteRepository.findByAula_IdAula(idAula);

        String tutorNuevo = null;
        for (String rol : rolesPorDocente.values()) {
            if ("TUTOR".equals(rol)) {
                tutorNuevo = rol;
                break;
            }
        }

        if (tutorNuevo != null) {
            for (AulaDocente ad : actuales) {
                if ("TUTOR".equals(ad.getRol())) {
                    ad.setActivo(false);
                    aulaDocenteRepository.save(ad);
                }
            }
        }

        for (AulaDocente ad : actuales) {
            if (!rolesPorDocente.containsKey(ad.getDocente().getIdDocente())) {
                ad.setActivo(false);
                aulaDocenteRepository.save(ad);
            } else {
                if (!ad.isActivo()) {
                    ad.setActivo(true);
                    aulaDocenteRepository.save(ad);
                }
            }
        }

        for (Map.Entry<Integer, String> entry : rolesPorDocente.entrySet()) {
            Integer idDocente = entry.getKey();
            String rol = entry.getValue();

            Docente docente = docenteRepository.findById(idDocente)
                    .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado"));


            var existente = aulaDocenteRepository.findByDocente_IdDocenteAndAula_IdAula(idDocente, idAula);
            if (existente.isPresent()) {
                AulaDocente ad = existente.get();
                ad.setActivo(true);
                ad.setRol(rol);
                aulaDocenteRepository.save(ad);
            } else {
                AulaDocente nuevaAsignacion = new AulaDocente(docente, aula, rol);
                aulaDocenteRepository.save(nuevaAsignacion);
            }
        }
    }
}
