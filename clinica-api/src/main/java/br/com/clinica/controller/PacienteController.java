package br.com.clinica.controller;

import br.com.clinica.dto.AgendamentoResponse;
import br.com.clinica.dto.PacienteRequest;
import br.com.clinica.dto.PacienteResponse;
import br.com.clinica.service.AgendamentoService;
import br.com.clinica.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;
    private final AgendamentoService agendamentoService;

    public PacienteController(PacienteService service, AgendamentoService agendamentoService) {
        this.service = service;
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> criar(@Valid @RequestBody PacienteRequest request) {
        PacienteResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    // GET /pacientes?nome=maria&cpf=12345678901  (parâmetros opcionais)
    @GetMapping
    public List<PacienteResponse> listar(@RequestParam(required = false) String nome,
                                         @RequestParam(required = false) String cpf) {
        return service.listar(nome, cpf);
    }

    @GetMapping("/{id}")
    public PacienteResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // Filtro no path: GET /pacientes/{id}/agendamentos
    @GetMapping("/{id}/agendamentos")
    public List<AgendamentoResponse> agendamentos(@PathVariable Long id) {
        return agendamentoService.listarPorPaciente(id);
    }

    @PutMapping("/{id}")
    public PacienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
