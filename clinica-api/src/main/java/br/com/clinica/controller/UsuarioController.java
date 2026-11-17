package br.com.clinica.controller;

import br.com.clinica.dto.UsuarioRequest;
import br.com.clinica.dto.UsuarioResponse;
import br.com.clinica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Gerenciamento de usuários (somente ADMIN). */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) {
        return service.criar(request);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }
}
