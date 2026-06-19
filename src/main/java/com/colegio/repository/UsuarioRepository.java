package com.colegio.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.colegio.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
   

	 Optional<Usuario> findTopByCodUsuarioStartingWithOrderByCodUsuarioDesc(String prefijo);
    
	

	Optional<Usuario> findByCodUsuario(String codUsuario);
	
	Optional<Usuario> findByCorreo(String correo);
	
}