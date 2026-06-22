package com.colegio.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.colegio.entity.Usuario;
import com.colegio.entity.Docente;
import com.colegio.entity.Alumno;
import com.colegio.repository.UsuarioRepository;
import com.colegio.repository.DocenteRepository;
import com.colegio.repository.AlumnoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;
	@Autowired
	private DocenteRepository docenteRepository;
	@Autowired
	private AlumnoRepository alumnoRepository;

	public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
							 DocenteRepository docenteRepository,
							 AlumnoRepository alumnoRepository) {
		this.usuarioRepository = usuarioRepository;
		this.docenteRepository = docenteRepository;
		this.alumnoRepository = alumnoRepository;
	}

	@Override
	public Usuario guardarUsuario(Usuario usuario) {


	    if (usuario.getIdUsuario() == 0) {
	        // Normalizar rol primero
	        if (usuario.getRol() != null) {
	            String r = usuario.getRol().trim();
	            if ("ADMINISTRADOR".equalsIgnoreCase(r)) {
	                usuario.setRol("ADMINISTRADOR");
	            } else if ("ALUMNO".equalsIgnoreCase(r) || "ESTUDIANTE".equalsIgnoreCase(r)) {
	                usuario.setRol("ESTUDIANTE");
	            } else if ("DOCENTE".equalsIgnoreCase(r)) {
	                usuario.setRol("DOCENTE");
	            } else {
	                usuario.setRol(r.toUpperCase());
	            }
	        }

	        // Determinar prefijo según el rol normalizado
	        char letraRol = obtenerLetraRol(usuario.getRol());
	        int anioActual = LocalDate.now().getYear();
	        String prefijo = letraRol + String.valueOf(anioActual);

	        // Buscar el último código con este prefijo para este rol específico
	        Optional<Usuario> ultimoOpt = usuarioRepository.findTopByCodUsuarioStartingWithOrderByCodUsuarioDesc(prefijo);
	        int siguienteNumero = 1;
	        if (ultimoOpt.isPresent()) {
	            String ultimoCodigo = ultimoOpt.get().getCodUsuario();
	            if (ultimoCodigo != null && ultimoCodigo.length() >= 6) {
	                // Extraer parte numérica: formato es LETRAAAOXXX
	                // Posición 1: Letra, Posición 2-5: Año, Posición 6-8: Número
	                String parteNumerica = ultimoCodigo.substring(5); 
	                try {
	                    siguienteNumero = Integer.parseInt(parteNumerica) + 1;
	                } catch (NumberFormatException e) {
	                    siguienteNumero = 1;
	                }
	            }
	        }

	        // Intentar generar un código único
	        int maxAttempts = 5;
	        for (int attempt = 0; attempt < maxAttempts; attempt++) {
	            String nuevoCodigo = String.format("%c%d%03d", letraRol, anioActual, siguienteNumero + attempt);
	            usuario.setCodUsuario(nuevoCodigo);
	            usuario.setEstado("ACTIVO");

	            try {
	                Usuario usuarioGuardado = usuarioRepository.save(usuario);
	                    
	                if ("DOCENTE".equals(usuarioGuardado.getRol())) {
	                	crearDocente(usuarioGuardado);
	                }
	                if ("ESTUDIANTE".equals(usuarioGuardado.getRol())) {
	                	crearAlumno(usuarioGuardado);
	                }
	                
	                return usuarioGuardado;
	            } catch (DataIntegrityViolationException ex) {
	                String rootMsg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
	                if (rootMsg != null && rootMsg.contains("cod_usuario")) {
	                    continue;
	                }
	                throw ex;
	            }
	        }
	        throw new IllegalStateException("No se pudo generar un codUsuario único tras varios intentos");
	    }

	    return usuarioRepository.save(usuario);
	}


	private void crearDocente(Usuario usuario) {
		try {
			if (docenteRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).isPresent()) {
				return;
			}
			
			String nombreCompleto = usuario.getNombreCompleto();
			if (nombreCompleto == null || nombreCompleto.isEmpty()) {
				nombreCompleto = "Profesor Sin Asignar";
			}
			
			Docente docente = new Docente(nombreCompleto, usuario);
			docenteRepository.save(docente);
		} catch (Exception e) {
			System.err.println("Error al crear Docente para usuario " + usuario.getIdUsuario() + ": " + e.getMessage());
		}
	}

	private void crearAlumno(Usuario usuario) {
		try {
			if (alumnoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).isPresent()) {
				return; 
			}
			Alumno alumno = new Alumno();
	        alumno.setUsuario(usuario);

	        if (usuario.getNombreCompleto() != null && !usuario.getNombreCompleto().isBlank()) {
	            alumno.setNombreCompleto(usuario.getNombreCompleto().trim());
	        } else {
	            alumno.setNombreCompleto("Estudiante Sin Asignar");
	        }

	        alumnoRepository.save(alumno);
	    } catch (Exception e) {
	        System.err.println("Error al crear Alumno para usuario " + usuario.getIdUsuario() + ": " + e.getMessage());
	    }
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario obtenerUsuarioPorId(int id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Usuario> buscarPorCodigo(String codUsuario) {
		return usuarioRepository.findByCodUsuario(codUsuario);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Usuario> listarTodos() {
		return usuarioRepository.findAll();
	}

	@Override
	public Usuario actualizarUsuario(int id, Usuario usuario) {
		Usuario existente = usuarioRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

		if (usuario.getNombreCompleto() != null) {
			existente.setNombreCompleto(usuario.getNombreCompleto());
		}

        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            existente.setPassword(usuario.getPassword());
        }

		if (usuario.getRol() != null) {
			existente.setRol(usuario.getRol());
		}

		if (usuario.getEstado() != null) {
			existente.setEstado(usuario.getEstado());
		}


		return usuarioRepository.save(existente);
	}

	@Override
	public void eliminarUsuario(int id) {
		Usuario existente = usuarioRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
		existente.setEstado("INACTIVO");
		usuarioRepository.save(existente);
	}

	/**
	 * Obtiene la letra de prefijo según el rol del usuario
	 * @param rol El rol del usuario (ADMINISTRADOR, DOCENTE, ESTUDIANTE)
	 * @return 'A' para ADMINISTRADOR, 'D' para DOCENTE, 'E' para ESTUDIANTE
	 */
	private char obtenerLetraRol(String rol) {
		if (rol == null) {
			return 'E'; // Valor por defecto
		}
		
		switch(rol.toUpperCase()) {
			case "ADMINISTRADOR":
				return 'A';
			case "DOCENTE":
				return 'D';
			case "ESTUDIANTE":
			case "ALUMNO":
				return 'E';
			default:
				return 'E'; // Valor por defecto para roles desconocidos
		}
	}

}
