package com.colegio.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.colegio.entity.Alumno;
import com.colegio.entity.Comunicado;
import com.colegio.entity.Docente;
import com.colegio.entity.Mensaje;
import com.colegio.entity.MensajeArchivo;
import com.colegio.entity.Usuario;
import com.colegio.repository.AlumnoRepository;
import com.colegio.repository.AulaDocenteRepository;
import com.colegio.repository.ComunicadoRepository;
import com.colegio.repository.DocenteRepository;
import com.colegio.repository.MensajeArchivoRepository;
import com.colegio.repository.UsuarioRepository;
import com.colegio.service.impl.MensajeService;

@Controller
@RequestMapping("/mensajes")
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private DocenteRepository docenteRepository;
    @Autowired
    private AulaDocenteRepository aulaDocenteRepository;
    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private MensajeArchivoRepository mensajeArchivoRepository;
    @Autowired
    private ComunicadoRepository comunicadoRepository;

    @GetMapping("/nuevo")
    public String nuevoMensaje(Model model, @RequestParam(value = "to", required = false) Integer to,
                                HttpSession session) {
        Usuario remitente = (Usuario) session.getAttribute("usuarioLogueado");
        Mensaje mensaje = new Mensaje();
        if (to != null) {
            usuarioRepository.findById(to).ifPresent(usuario -> mensaje.setDestinatario(usuario));
        }
        model.addAttribute("mensaje", mensaje);

        List<Usuario> destinatarios;

        if (remitente != null && "ESTUDIANTE".equalsIgnoreCase(remitente.getRol())) {
            destinatarios = usuarioRepository.findAll().stream()
                    .filter(u -> "DOCENTE".equalsIgnoreCase(u.getRol()))
                    .collect(Collectors.toList());
        } else if (remitente != null && "DOCENTE".equalsIgnoreCase(remitente.getRol())) {
            destinatarios = docenteRepository.findByUsuario_IdUsuario(remitente.getIdUsuario())
                    .map(docente -> {
                        List<Integer> aulaIds = aulaDocenteRepository.findAulaIdsByDocenteId(docente.getIdDocente());
                        return aulaIds.stream()
                                .flatMap(aulaId -> alumnoRepository.findByAula_IdAula(aulaId).stream())
                                .map(Alumno::getUsuario)
                                .filter(u -> u != null && "ESTUDIANTE".equalsIgnoreCase(u.getRol()))
                                .distinct()
                                .collect(Collectors.toList());
                    })
                    .orElseGet(List::of);
        } else {
            destinatarios = usuarioRepository.findAll().stream()
                    .filter(u -> "DOCENTE".equalsIgnoreCase(u.getRol()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("docentes", destinatarios);
        return "mensajes/nuevo";
    }

    @PostMapping("/enviar")
    public String enviarMensaje(Mensaje mensaje,
                                @RequestParam(value = "files", required = false) MultipartFile[] archivos,
                                HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            redirectAttrs.addFlashAttribute("mensajeError", "Debes iniciar sesión para enviar mensajes.");
            return "redirect:/login";
        }
        try {
            mensaje.setRemitente(u);

            if (archivos != null && archivos.length > 0) {
                long totalSize = 0;
                List<MensajeArchivo> listaArchivos = new ArrayList<>();
                for (MultipartFile f : archivos) {
                    if (f == null || f.isEmpty()) continue;
                    totalSize += f.getSize();
                    if (totalSize > 20 * 1024 * 1024) {
                        throw new IllegalArgumentException("El tamaño total de los archivos supera los 20 MB.");
                    }
                    MensajeArchivo ma = new MensajeArchivo();
                    ma.setArchivoData(f.getBytes());
                    ma.setArchivoNombre(f.getOriginalFilename());
                    ma.setArchivoTipo(f.getContentType());
                    ma.setMensaje(mensaje);
                    listaArchivos.add(ma);
                }
                mensaje.setArchivos(listaArchivos);
            }

            mensajeService.enviarMensaje(mensaje);
            redirectAttrs.addFlashAttribute("mensajeExito", "Mensaje enviado correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/mensajes/inbox";
    }

    @GetMapping("/inbox")
    public String inbox(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        List<Mensaje> inbox = mensajeService.listarInbox(u.getIdUsuario());
        model.addAttribute("inbox", inbox);
        model.addAttribute("currentUserId", u.getIdUsuario());
        return "mensajes/inbox";
    }

    @GetMapping("/contactos")
    public String contactos(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        List<Usuario> contactos = mensajeService.listarContactos(u.getIdUsuario());
        model.addAttribute("contactos", contactos);
        model.addAttribute("currentUserId", u.getIdUsuario());
        return "mensajes/contactos";
    }

    @GetMapping("/chat/{conUserId}")
    public String chat(@PathVariable int conUserId, HttpSession session, Model model,
                       @RequestParam(value = "refTitulo", required = false) String refTitulo,
                       @RequestParam(value = "refId", required = false) Integer refId) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        Usuario contacto = usuarioRepository.findById(conUserId).orElse(null);
        if (contacto == null) return "redirect:/mensajes/inbox";
        List<Mensaje> conversacion = mensajeService.obtenerConversacion(u.getIdUsuario(), conUserId);
        model.addAttribute("contacto", contacto);
        model.addAttribute("conversacion", conversacion);
        model.addAttribute("currentUserId", u.getIdUsuario());
        model.addAttribute("refTitulo", refTitulo);
        model.addAttribute("refId", refId);
        return "mensajes/chat";
    }

    @PostMapping("/chat/enviar")
    public Object enviarChat(@RequestParam("destinatarioId") int destinatarioId,
                             @RequestParam("contenido") String contenido,
                             @RequestParam(value = "files", required = false) MultipartFile[] archivos,
                             @RequestParam(value = "refId", required = false) Integer refId,
                             HttpSession session, HttpServletRequest request) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        try {
            Usuario destinatario = usuarioRepository.findById(destinatarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Destinatario no encontrado."));
            Mensaje m = new Mensaje();
            m.setRemitente(u);
            m.setDestinatario(destinatario);
            m.setContenido(contenido);

            if (refId != null) {
                comunicadoRepository.findById(refId).ifPresent(c -> m.setComunicadoReferencia(c));
            }

            if (archivos != null && archivos.length > 0) {
                long totalSize = 0;
                List<MensajeArchivo> listaArchivos = new ArrayList<>();
                for (MultipartFile f : archivos) {
                    if (f == null || f.isEmpty()) continue;
                    totalSize += f.getSize();
                    if (totalSize > 20 * 1024 * 1024) {
                        throw new IllegalArgumentException("El tamaño total de los archivos supera los 20 MB.");
                    }
                    MensajeArchivo ma = new MensajeArchivo();
                    ma.setArchivoData(f.getBytes());
                    ma.setArchivoNombre(f.getOriginalFilename());
                    ma.setArchivoTipo(f.getContentType());
                    ma.setMensaje(m);
                    listaArchivos.add(ma);
                }
                m.setArchivos(listaArchivos);
            }

            Mensaje saved = mensajeService.enviarMensaje(m);

            if (isAjax) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("id", saved.getIdMensaje());
                result.put("contenido", saved.getContenido());
                result.put("fechaEnvio", saved.getFechaEnvio() != null ? saved.getFechaEnvio().format(fmt) : "");
                result.put("remitenteId", u.getIdUsuario());
                result.put("remitenteNombre", u.getNombreCompleto());

                if (saved.getComunicadoReferencia() != null) {
                    result.put("refId", saved.getComunicadoReferencia().getIdComunicado());
                    result.put("refTitulo", saved.getComunicadoReferencia().getTitulo());
                }

                List<Map<String, Object>> archivosJson = new ArrayList<>();
                if (saved.getArchivos() != null) {
                    for (MensajeArchivo ma : saved.getArchivos()) {
                        Map<String, Object> am = new java.util.HashMap<>();
                        am.put("idArchivo", ma.getIdArchivo());
                        am.put("archivoNombre", ma.getArchivoNombre());
                        am.put("archivoTipo", ma.getArchivoTipo());
                        archivosJson.add(am);
                    }
                }
                result.put("archivos", archivosJson);

                return ResponseEntity.ok(result);
            }

            return "redirect:/mensajes/chat/" + destinatarioId;
        } catch (Exception e) {
            if (isAjax) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            return "redirect:/mensajes/chat/" + destinatarioId;
        }
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable int id, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).build();
        try {
            Mensaje m = mensajeService.obtenerPorId(id, u.getIdUsuario());
            if (m == null || m.getArchivos() == null || m.getArchivos().isEmpty())
                return ResponseEntity.badRequest().build();
            MensajeArchivo ma = m.getArchivos().get(0);
            String tipo = ma.getArchivoTipo() != null ? ma.getArchivoTipo() : "application/octet-stream";
            ByteArrayResource resource = new ByteArrayResource(ma.getArchivoData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ma.getArchivoNombre() + "\"")
                    .contentType(MediaType.parseMediaType(tipo))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/abrir/{id}")
    public ResponseEntity<Resource> abrirArchivo(@PathVariable int id, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).build();
        try {
            Mensaje m = mensajeService.obtenerPorId(id, u.getIdUsuario());
            if (m == null || m.getArchivos() == null || m.getArchivos().isEmpty())
                return ResponseEntity.badRequest().build();
            MensajeArchivo ma = m.getArchivos().get(0);
            String tipo = ma.getArchivoTipo() != null ? ma.getArchivoTipo() : "application/octet-stream";
            ByteArrayResource resource = new ByteArrayResource(ma.getArchivoData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + ma.getArchivoNombre() + "\"")
                    .contentType(MediaType.parseMediaType(tipo))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/descargarArchivo/{idArchivo}")
    public ResponseEntity<Resource> descargarArchivoPorId(@PathVariable int idArchivo, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).build();
        try {
            MensajeArchivo ma = mensajeArchivoRepository.findById(idArchivo).orElse(null);
            if (ma == null) return ResponseEntity.badRequest().build();
            String tipo = ma.getArchivoTipo() != null ? ma.getArchivoTipo() : "application/octet-stream";
            ByteArrayResource resource = new ByteArrayResource(ma.getArchivoData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ma.getArchivoNombre() + "\"")
                    .contentType(MediaType.parseMediaType(tipo))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/abrirArchivo/{idArchivo}")
    public ResponseEntity<Resource> abrirArchivoPorId(@PathVariable int idArchivo, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).build();
        try {
            MensajeArchivo ma = mensajeArchivoRepository.findById(idArchivo).orElse(null);
            if (ma == null) return ResponseEntity.badRequest().build();
            String tipo = ma.getArchivoTipo() != null ? ma.getArchivoTipo() : "application/octet-stream";
            ByteArrayResource resource = new ByteArrayResource(ma.getArchivoData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + ma.getArchivoNombre() + "\"")
                    .contentType(MediaType.parseMediaType(tipo))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/no-leidos")
    @ResponseBody
    public ResponseEntity<?> noLeidos(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        long count = mensajeService.contarNoLeidos(u.getIdUsuario());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/ver/{id}")
    @ResponseBody
    public ResponseEntity<?> verMensaje(@PathVariable int id, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).body("No autenticado");
        try {
            Mensaje m = mensajeService.obtenerPorId(id, u.getIdUsuario());
            if (m == null) return ResponseEntity.badRequest().body("Mensaje no encontrado.");
            if (m.getDestinatario() != null && m.getDestinatario().getIdUsuario().intValue() == u.getIdUsuario().intValue()) {
                mensajeService.marcarLeido(id, u.getIdUsuario());
            }
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
