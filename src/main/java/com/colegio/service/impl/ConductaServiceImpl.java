package com.colegio.service.impl;

import com.colegio.entity.Conducta;
import com.colegio.repository.ConductaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ConductaServiceImpl implements ConductaService {

    @Autowired
    private ConductaRepository conductaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Conducta> listarPorAlumno(Integer idAlumno) {
        return conductaRepository.findByAlumno_IdAlumno(idAlumno);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conducta> listarPorAlumnoYBimestre(Integer idAlumno, Integer bimestre) {
        if (bimestre == null || bimestre <= 0) {
            return conductaRepository.findByAlumno_IdAlumno(idAlumno);
        }
        return conductaRepository.findByAlumno_IdAlumnoAndBimestre(idAlumno, bimestre);
    }

    @Override
    @Transactional
    public Conducta guardar(Conducta conducta) {
        if (conducta.getFechaRegistro() == null) {
            conducta.setFechaRegistro(LocalDate.now());
        }
        if (conducta.getAnio() == null) {
            conducta.setAnio(LocalDate.now().getYear());
        }
        return conductaRepository.save(conducta);
    }

    @Override
    @Transactional
    public Conducta actualizar(Conducta conducta) {
        return conductaRepository.save(conducta);
    }

    @Override
    @Transactional
    public void eliminar(Integer idConducta) {
        conductaRepository.deleteById(idConducta);
    }

    @Override
    @Transactional(readOnly = true)
    public Conducta obtenerPorId(Integer idConducta) {
        return conductaRepository.findById(idConducta).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorAlumnoYTipo(Integer idAlumno, String tipo) {
        return conductaRepository.countByAlumno_IdAlumnoAndTipo(idAlumno, tipo);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorAlumnoYTipoYBimestre(Integer idAlumno, String tipo, Integer bimestre) {
        if (bimestre == null || bimestre <= 0) {
            return conductaRepository.countByAlumno_IdAlumnoAndTipo(idAlumno, tipo);
        }
        return conductaRepository.countByAlumno_IdAlumnoAndTipoAndBimestre(idAlumno, tipo, bimestre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conducta> listarUltimasPorDocente(Integer idDocente) {
        return conductaRepository.findByDocente_IdDocenteOrderByFechaRegistroDesc(idDocente);
    }
}
