package com.colegio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.colegio.entity.Mensaje;

public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {
    List<Mensaje> findByDestinatario_IdUsuarioOrderByFechaEnvioDesc(int idUsuario);
    List<Mensaje> findByRemitente_IdUsuarioOrderByFechaEnvioDesc(int idUsuario);
    long countByDestinatario_IdUsuarioAndLeidoFalse(int idUsuario);

    @Query("SELECT DISTINCT m FROM Mensaje m LEFT JOIN FETCH m.archivos WHERE (m.remitente.idUsuario = :user1 AND m.destinatario.idUsuario = :user2) OR (m.remitente.idUsuario = :user2 AND m.destinatario.idUsuario = :user1) ORDER BY m.fechaEnvio ASC")
    List<Mensaje> findConversacion(@Param("user1") int user1, @Param("user2") int user2);

    @Query("SELECT DISTINCT u FROM Mensaje m JOIN Usuario u ON (m.remitente.idUsuario = u.idUsuario OR m.destinatario.idUsuario = u.idUsuario) WHERE (m.remitente.idUsuario = :userId OR m.destinatario.idUsuario = :userId) AND u.idUsuario <> :userId")
    List<com.colegio.entity.Usuario> findContactos(@Param("userId") int userId);

    @Query("SELECT DISTINCT m FROM Mensaje m LEFT JOIN FETCH m.archivos WHERE m.idMensaje = :id")
    java.util.Optional<Mensaje> findByIdWithArchivos(@Param("id") int id);
}
