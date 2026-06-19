package com.colegio.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.colegio.entity.Aula;
import com.colegio.repository.AulaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class AulaServiceImpl implements AulaService {

	@Autowired
	private AulaRepository aulaRepository;

	public AulaServiceImpl(AulaRepository aulaRepository) {
		this.aulaRepository = aulaRepository;
	}

	@Override
	public Aula guardarAula(Aula aula) {
		if (aulaRepository.existsByGradoAndSeccion(aula.getGrado(), aula.getSeccion())) {
			throw new IllegalArgumentException("Ya existe el aula: " + aula.getGrado() + " - " + aula.getSeccion());
		}
		return aulaRepository.save(aula);
	}

	@Override
	@Transactional(readOnly = true)
	public Aula obtenerAulaPorId(int id) {
		return aulaRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Aula no encontrada con ID: " + id));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Aula> buscarPorGradoYSeccion(String grado, String seccion) {
		return aulaRepository.buscarPorGradoYSeccion(grado, seccion);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Aula> listarTodas() {
		return aulaRepository.findAll();
	}

	@Override
	public Aula actualizarAula(int id, Aula aula) {
		Aula existente = aulaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Aula no encontrada con ID: " + id));

        String nuevoGrado = aula.getGrado();
        String nuevaSeccion = aula.getSeccion();

        if (nuevoGrado != null && nuevaSeccion != null) {
            Optional<Aula> encontrada = aulaRepository.buscarPorGradoYSeccion(nuevoGrado, nuevaSeccion);
            if (encontrada.isPresent() && encontrada.get().getIdAula() != id) {
                throw new IllegalArgumentException("Otra aula ya ocupa el grado y sección: " + nuevoGrado + " - " + nuevaSeccion);
            }
            existente.setGrado(nuevoGrado);
            existente.setSeccion(nuevaSeccion);
        }

        if (aula.getCapacidad() > 0) {
            existente.setCapacidad(aula.getCapacidad());
        }

        return aulaRepository.save(existente);
    }


	@Override
	public void eliminarAula(int id) {
		if (!aulaRepository.existsById(id)) {
            throw new EntityNotFoundException("Aula no encontrada con ID: " + id);
        }
        aulaRepository.deleteById(id);

	}

}
