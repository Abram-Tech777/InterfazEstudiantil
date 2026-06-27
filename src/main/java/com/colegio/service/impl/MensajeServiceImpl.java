package com.colegio.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.colegio.entity.Mensaje;
import com.colegio.entity.Usuario;
import com.colegio.repository.MensajeRepository;

@Service
@Transactional
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;

    public MensajeServiceImpl(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    @Override
    public Mensaje enviarMensaje(Mensaje mensaje) {
        if (mensaje.getRemitente() == null || mensaje.getDestinatario() == null) {
            throw new IllegalArgumentException("Remitente y destinatario son obligatorios.");
        }
        if ((mensaje.getContenido() == null || mensaje.getContenido().isBlank()) &&
            (mensaje.getArchivos() == null || mensaje.getArchivos().isEmpty())) {
            throw new IllegalArgumentException("El mensaje debe tener contenido o al menos un archivo adjunto.");
        }
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensaje.setLeido(false);
        return mensajeRepository.save(mensaje);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mensaje> listarInbox(int idUsuario) {
        return mensajeRepository.findInboxByUserId(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mensaje> listarEnviados(int idUsuario) {
        return mensajeRepository.findByRemitente_IdUsuarioOrderByFechaEnvioDesc(idUsuario);
    }

    @Override
    public Mensaje marcarLeido(int idMensaje, int idUsuario) {
        Mensaje m = mensajeRepository.findById(idMensaje).orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado."));
        if (m.getDestinatario() == null || m.getDestinatario().getIdUsuario() != idUsuario) {
            throw new IllegalArgumentException("No tiene permiso para marcar este mensaje.");
        }
        m.setLeido(true);
        return mensajeRepository.save(m);
    }

    @Override
    @Transactional(readOnly = true)
    public Mensaje obtenerPorId(int idMensaje, int idUsuario) {
        Mensaje m = mensajeRepository.findByIdWithArchivos(idMensaje).orElse(null);
        if (m == null) return null;
        if ((m.getRemitente() != null && m.getRemitente().getIdUsuario() == idUsuario) ||
            (m.getDestinatario() != null && m.getDestinatario().getIdUsuario() == idUsuario)) {
            return m;
        }
        throw new IllegalArgumentException("No tiene permiso para ver este mensaje.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mensaje> obtenerConversacion(int idUsuario1, int idUsuario2) {
        return mensajeRepository.findConversacion(idUsuario1, idUsuario2);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarContactos(int idUsuario) {
        return mensajeRepository.findContactos(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidos(int idUsuario) {
        return mensajeRepository.countNoLeidosFormales(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mensaje> obtenerNuevosDesde(int idUsuario1, int idUsuario2, java.time.LocalDateTime after) {
        return mensajeRepository.findNuevosDesde(idUsuario1, idUsuario2, after);
    }

    @Override
    public void eliminarMensaje(int idMensaje, int idUsuario) {
        Mensaje m = mensajeRepository.findById(idMensaje)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado."));
        if ((m.getDestinatario() == null || m.getDestinatario().getIdUsuario() != idUsuario) &&
            (m.getRemitente() == null || m.getRemitente().getIdUsuario() != idUsuario)) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este mensaje.");
        }
        mensajeRepository.delete(m);
    }

    @Override
    public void vaciarInbox(int idUsuario) {
        List<Mensaje> inbox = mensajeRepository.findInboxByUserId(idUsuario);
        mensajeRepository.deleteAll(inbox);
    }

    @Override
    public void eliminarConversacion(int userId, int conUserId) {
        List<Mensaje> conversacion = mensajeRepository.findConversacion(userId, conUserId);
        mensajeRepository.deleteAll(conversacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mensaje> obtenerRespuestas(int parentId) {
        return mensajeRepository.findRespuestasByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Long> contarNoLeidosPorContacto(int userId) {
        List<Usuario> contactos = mensajeRepository.findContactos(userId);
        Map<Integer, Long> resultado = new HashMap<>();
        for (Usuario c : contactos) {
            long count = mensajeRepository.countByDestinatario_IdUsuarioAndLeidoFalse(userId);
            // Count only messages FROM this contacto that are unread
            long fromContacto = mensajeRepository.countByRemitente_IdUsuarioAndDestinatario_IdUsuarioAndLeidoFalse(c.getIdUsuario(), userId);
            if (fromContacto > 0) {
                resultado.put(c.getIdUsuario(), fromContacto);
            }
        }
        return resultado;
    }
}
