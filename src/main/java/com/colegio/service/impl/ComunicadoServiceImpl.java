package com.colegio.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.colegio.entity.Alumno;
import com.colegio.entity.Comunicado;
import com.colegio.repository.ComunicadoRepository;

@Service
@Transactional
public class ComunicadoServiceImpl implements ComunicadoService {

    private final ComunicadoRepository comunicadoRepository;

    public ComunicadoServiceImpl(ComunicadoRepository comunicadoRepository) {
        this.comunicadoRepository = comunicadoRepository;
    }

    @Override
    public Comunicado crearComunicado(Comunicado comunicado) {
        if (comunicado.getTitulo() == null || comunicado.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío.");
        }
        if (comunicado.getContenido() == null || comunicado.getContenido().isBlank()) {
            throw new IllegalArgumentException("El contenido no puede estar vacío.");
        }
        if (comunicado.getFechaEmision() == null) {
            comunicado.setFechaEmision(LocalDateTime.now());
        }
        return comunicadoRepository.save(comunicado);
    }

    @Override
    public List<Comunicado> listarPorAula(int idAula) {
        return comunicadoRepository.listarPorAula(idAula);
    }

    @Override
    public List<Comunicado> listarParaAlumno(Alumno alumno) {
        if (alumno == null || alumno.getAula() == null) return List.of();
        int idAula = alumno.getAula().getIdAula();
        String grado = alumno.getAula().getGrado();
        return comunicadoRepository.listarParaAulaOGrado(idAula, grado);
    }

    @Override
    public List<Comunicado> listarTodos() {
        return comunicadoRepository.listarTodosOrdenados();
    }

    @Override
    public void eliminarComunicado(Integer idComunicado) {
        comunicadoRepository.deleteById(idComunicado);
    }
}
