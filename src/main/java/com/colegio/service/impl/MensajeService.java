package com.colegio.service.impl;

import java.util.List;
import java.util.Map;

import com.colegio.entity.Mensaje;
import com.colegio.entity.Usuario;

public interface MensajeService {
    Mensaje enviarMensaje(Mensaje mensaje);
    List<Mensaje> listarInbox(int idUsuario);
    List<Mensaje> listarEnviados(int idUsuario);
    Mensaje marcarLeido(int idMensaje, int idUsuario);
    Mensaje obtenerPorId(int idMensaje, int idUsuario);
    List<Mensaje> obtenerConversacion(int idUsuario1, int idUsuario2);
    List<Usuario> listarContactos(int idUsuario);
    long contarNoLeidos(int idUsuario);
    List<Mensaje> obtenerNuevosDesde(int idUsuario1, int idUsuario2, java.time.LocalDateTime after);
    void eliminarMensaje(int idMensaje, int idUsuario);
    void vaciarInbox(int idUsuario);
    void eliminarConversacion(int userId, int conUserId);
    List<Mensaje> obtenerRespuestas(int parentId);
    Map<Integer, Long> contarNoLeidosPorContacto(int userId);
}
