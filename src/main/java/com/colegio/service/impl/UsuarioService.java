package com.colegio.service.impl;

import java.util.List;
import java.util.Optional;

import com.colegio.entity.Usuario;


public interface UsuarioService {
	Usuario guardarUsuario(Usuario usuario);
   
	Usuario obtenerUsuarioPorId(int id);
   
	Optional<Usuario> buscarPorCodigo(String username);
  
	List<Usuario> listarTodos();
   
	Usuario actualizarUsuario(int id, Usuario usuario);
    void eliminarUsuario(int id);
}
