package com.colegio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.colegio.entity.Mensaje;

public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {
    List<Mensaje> findByDestinatario_IdUsuarioOrderByFechaEnvioDesc(int idUsuario);
    List<Mensaje> findByRemitente_IdUsuarioOrderByFechaEnvioDesc(int idUsuario);

    @Query("SELECT m FROM Mensaje m WHERE m.mensajePadre IS NULL AND ((m.destinatario.idUsuario = :userId AND (m.remitente.rol = 'DOCENTE' OR m.remitente.rol = 'ADMINISTRADOR')) OR m.remitente.idUsuario = :userId) ORDER BY m.fechaEnvio DESC")
    List<Mensaje> findInboxByUserId(@Param("userId") int userId);

    @Query("SELECT DISTINCT m FROM Mensaje m LEFT JOIN FETCH m.archivos WHERE m.mensajePadre.idMensaje = :parentId ORDER BY m.fechaEnvio ASC")
    List<Mensaje> findRespuestasByParentId(@Param("parentId") int parentId);
    long countByDestinatario_IdUsuarioAndLeidoFalse(int idUsuario);
    long countByRemitente_IdUsuarioAndDestinatario_IdUsuarioAndLeidoFalse(int remitenteId, int destinatarioId);

    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.destinatario.idUsuario = :userId AND m.leido = false AND (m.remitente.rol = 'DOCENTE' OR m.remitente.rol = 'ADMINISTRADOR')")
    long countNoLeidosFormales(@Param("userId") int userId);

    @Query("SELECT DISTINCT m FROM Mensaje m LEFT JOIN FETCH m.archivos LEFT JOIN FETCH m.comunicadoReferencia WHERE (m.remitente.idUsuario = :user1 AND m.destinatario.idUsuario = :user2) OR (m.remitente.idUsuario = :user2 AND m.destinatario.idUsuario = :user1) ORDER BY m.fechaEnvio ASC")
    List<Mensaje> findConversacion(@Param("user1") int user1, @Param("user2") int user2);

    @Query("SELECT DISTINCT u FROM Mensaje m JOIN Usuario u ON (m.remitente.idUsuario = u.idUsuario OR m.destinatario.idUsuario = u.idUsuario) WHERE (m.remitente.idUsuario = :userId OR m.destinatario.idUsuario = :userId) AND u.idUsuario <> :userId")
    List<com.colegio.entity.Usuario> findContactos(@Param("userId") int userId);

    @Query("SELECT DISTINCT m FROM Mensaje m LEFT JOIN FETCH m.archivos LEFT JOIN FETCH m.comunicadoReferencia WHERE m.idMensaje = :id")
    java.util.Optional<Mensaje> findByIdWithArchivos(@Param("id") int id);

    @Query("SELECT DISTINCT m FROM Mensaje m LEFT JOIN FETCH m.archivos LEFT JOIN FETCH m.comunicadoReferencia WHERE ((m.remitente.idUsuario = :user1 AND m.destinatario.idUsuario = :user2) OR (m.remitente.idUsuario = :user2 AND m.destinatario.idUsuario = :user1)) AND m.fechaEnvio > :after ORDER BY m.fechaEnvio ASC")
    List<Mensaje> findNuevosDesde(@Param("user1") int user1, @Param("user2") int user2, @Param("after") java.time.LocalDateTime after);
}
