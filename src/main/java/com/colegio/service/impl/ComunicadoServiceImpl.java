package com.colegio.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.ByteArrayResource;

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

    @Override
    public String guardarArchivo(MultipartFile archivo) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío.");
        }

        // Validar extensiones permitidas
        String nombreArchivo = archivo.getOriginalFilename();
        String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".")).toLowerCase();
        
        String[] extensionesPermitidas = {".pdf", ".doc", ".docx", ".jpg", ".jpeg", ".png", ".gif"};
        boolean extensionValida = false;
        for (String ext : extensionesPermitidas) {
            if (extension.equals(ext)) {
                extensionValida = true;
                break;
            }
        }
        
        if (!extensionValida) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo se aceptan: PDF, Word, imágenes.");
        }

        // Crear directorio si no existe
        Path uploadDir = Paths.get("uploads/comunicados");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Generar nombre único para el archivo
        String nombreUnico = UUID.randomUUID().toString() + extension;
        Path rutaArchivo = uploadDir.resolve(nombreUnico);

        // Guardar archivo en el servidor
        Files.write(rutaArchivo, archivo.getBytes());

        // Retornar ruta relativa para acceso desde web
        return "/uploads/comunicados/" + nombreUnico;
    }

    @Override
    public ResponseEntity<Resource> descargarArchivo(Integer idComunicado) throws Exception {
        Comunicado comunicado = comunicadoRepository.findById(idComunicado)
                .orElseThrow(() -> new IllegalArgumentException("Comunicado no encontrado."));
        
        if (comunicado.getArchivoData() == null || comunicado.getArchivoData().length == 0) {
            throw new IllegalArgumentException("El archivo no existe.");
        }

        ByteArrayResource resource = new ByteArrayResource(comunicado.getArchivoData());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + comunicado.getArchivoNombre() + "\"")
                .contentType(MediaType.parseMediaType(comunicado.getArchivoTipo()))
                .body(resource);
    }

    @Override
    public Comunicado obtenerComunicadoPorId(Integer id) {
        return comunicadoRepository.findById(id).orElse(null);
    }
}
