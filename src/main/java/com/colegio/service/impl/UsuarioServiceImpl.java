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
	        int anioActual = LocalDate.now().getYear();
	        String prefijo = "C" + anioActual; 


	        Optional<Usuario> ultimoOpt = usuarioRepository.findTopByCodUsuarioStartingWithOrderByCodUsuarioDesc(prefijo);
            int siguienteNumero = 1;
	        if (ultimoOpt.isPresent()) {
	            String ultimoCodigo = ultimoOpt.get().getCodUsuario();
	            if (ultimoCodigo != null && ultimoCodigo.length() >= 8) {
	                String parteNumerica = ultimoCodigo.substring(5); 
	                try {
	                    siguienteNumero = Integer.parseInt(parteNumerica) + 1;
	                } catch (NumberFormatException e) {
	                    siguienteNumero = 1;
	                }
	            }
	        }


	        int maxAttempts = 5;
	        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String nuevoCodigo = String.format("C%d%03d", anioActual, siguienteNumero + attempt);
            usuario.setCodUsuario(nuevoCodigo);
            usuario.setEstado("ACTIVO");

            if (usuario.getRol() != null) {
                String r = usuario.getRol().trim();
                if ("ADMINISTRADOR".equalsIgnoreCase(r) || "ADMINISTRADOR".equalsIgnoreCase(r)) {
                    usuario.setRol("ADMINISTRADOR");
                } else if ("ALUMNO".equalsIgnoreCase(r) || "ESTUDIANTE".equalsIgnoreCase(r)) {
                    usuario.setRol("ESTUDIANTE");
                } else if ("DOCENTE".equalsIgnoreCase(r)) {
                    usuario.setRol("DOCENTE");
                } else {
                    usuario.setRol(r.toUpperCase());
                }
            }

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
			
			if (usuario.getNombreCompleto() != null && !usuario.getNombreCompleto().isEmpty()) {
				String[] partes = usuario.getNombreCompleto().split(" ", 2);
				alumno.setNombre(partes[0]);
				if (partes.length > 1) {
					alumno.setApellido(partes[1]);
				} else {
					alumno.setApellido(partes[0]);
				}
			} else {
				alumno.setNombre("Estudiante");
				alumno.setApellido("Sin Asignar");
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

		if (usuario.getPassword() != null) {
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

}
