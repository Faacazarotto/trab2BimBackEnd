package br.com.clinica.service;

import br.com.clinica.dto.UsuarioRequest;
import br.com.clinica.dto.UsuarioResponse;
import br.com.clinica.exception.ConflitoException;
import br.com.clinica.model.Usuario;
import br.com.clinica.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest r) {
        if (repository.existsByLogin(r.login())) {
            throw new ConflitoException("Já existe um usuário com este login");
        }
        Usuario u = new Usuario(r.login(), passwordEncoder.encode(r.senha()), r.perfil());
        return UsuarioResponse.de(repository.save(u));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(UsuarioResponse::de).toList();
    }
}
