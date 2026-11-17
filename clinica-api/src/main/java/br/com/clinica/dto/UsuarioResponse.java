package br.com.clinica.dto;

import br.com.clinica.model.Perfil;
import br.com.clinica.model.Usuario;

public record UsuarioResponse(Long id, String login, Perfil perfil) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getLogin(), u.getPerfil());
    }
}
