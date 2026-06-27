package com.colegio.service.impl;

import java.util.List;

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
}
