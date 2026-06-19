package com.colegio.service.impl;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.colegio.entity.Docente;
import com.colegio.repository.DocenteRepository;

@Service
@Transactional
public class DocenteServiceImpl implements DocenteService {

    private final DocenteRepository docenteRepository;

    public DocenteServiceImpl(DocenteRepository docenteRepository) {
        this.docenteRepository = docenteRepository;
    }

	@Override
	public Docente crearDocente(Docente docente) {
		try {
			return docenteRepository.save(docente);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException("Violación de integridad: " + ex.getMostSpecificCause().getMessage(), ex);
		}
	}

    @Override
    @Transactional(readOnly = true)
    public Docente getDocenteById(int id) {
        return docenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

	@Override
	public Docente actualizarDocente(int id, Docente docente) {
		Docente existente = docenteRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Docente no encontrado con id: " + id));

		if (docente.getNombreCompleto() != null) existente.setNombreCompleto(docente.getNombreCompleto());
		if (docente.getUsuario() != null) existente.setUsuario(docente.getUsuario());

		return docenteRepository.save(existente);
	}

    @Override
    public void eliminarDocente(int id) {
        if (!docenteRepository.existsById(id)) {
            throw new EntityNotFoundException("Docente no encontrado con id: " + id);
        }
        docenteRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Docente> findByUsuarioId(int idUsuario) {
        return docenteRepository.findByUsuario_IdUsuario(idUsuario);
    }
}