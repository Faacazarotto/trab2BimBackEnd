package br.com.clinica.controller;

import br.com.clinica.dto.AgendamentoResponse;
import br.com.clinica.dto.ProfissionalRequest;
import br.com.clinica.dto.ProfissionalResponse;
import br.com.clinica.service.AgendamentoService;
import br.com.clinica.service.ProfissionalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;
    private final AgendamentoService agendamentoService;

    public ProfissionalController(ProfissionalService service, AgendamentoService agendamentoService) {
        this.service = service;
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public ResponseEntity<ProfissionalResponse> criar(@Valid @RequestBody ProfissionalRequest request) {
        ProfissionalResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    // GET /profissionais?nome=ana&especialidade=cardio  (parâmetros opcionais)
    @GetMapping
    public List<ProfissionalResponse> listar(@RequestParam(required = false) String nome,
                                             @RequestParam(required = false) String especialidade) {
        return service.listar(nome, especialidade);
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // Filtro no path: GET /profissionais/{id}/agendamentos
    @GetMapping("/{id}/agendamentos")
    public List<AgendamentoResponse> agendamentos(@PathVariable Long id) {
        return agendamentoService.listarPorProfissional(id);
    }

    @PutMapping("/{id}")
    public ProfissionalResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProfissionalRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
