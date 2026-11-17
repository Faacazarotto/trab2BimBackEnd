package br.com.clinica.service;

import br.com.clinica.dto.PacienteRequest;
import br.com.clinica.dto.PacienteResponse;
import br.com.clinica.exception.ConflitoException;
import br.com.clinica.exception.RecursoNaoEncontradoException;
import br.com.clinica.model.Paciente;
import br.com.clinica.repository.AgendamentoRepository;
import br.com.clinica.repository.PacienteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository repository;
    private final AgendamentoRepository agendamentoRepository;

    public PacienteService(PacienteRepository repository, AgendamentoRepository agendamentoRepository) {
        this.repository = repository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional
    public PacienteResponse criar(PacienteRequest r) {
        if (repository.existsByCpf(r.cpf())) {
            throw new ConflitoException("Já existe um paciente cadastrado com este CPF");
        }
        Paciente p = new Paciente(r.nome(), r.cpf(), r.email(), r.telefone(), r.dataNascimento());
        return PacienteResponse.de(repository.save(p));
    }

    /** Consulta com múltiplos parâmetros opcionais: nome (parcial) e cpf (exato). */
    @Transactional(readOnly = true)
    public List<PacienteResponse> listar(String nome, String cpf) {
        Specification<Paciente> spec = (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%"));
        }
        if (cpf != null && !cpf.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("cpf"), cpf.trim()));
        }

        return repository.findAll(spec, Sort.by("nome")).stream().map(PacienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscar(Long id) {
        return PacienteResponse.de(obter(id));
    }

    @Transactional
    public PacienteResponse atualizar(Long id, PacienteRequest r) {
        Paciente p = obter(id);
        if (repository.existsByCpfAndIdNot(r.cpf(), id)) {
            throw new ConflitoException("Já existe outro paciente cadastrado com este CPF");
        }
        p.setNome(r.nome());
        p.setCpf(r.cpf());
        p.setEmail(r.email());
        p.setTelefone(r.telefone());
        p.setDataNascimento(r.dataNascimento());
        return PacienteResponse.de(repository.save(p));
    }

    @Transactional
    public void excluir(Long id) {
        Paciente p = obter(id);
        if (agendamentoRepository.existsByPacienteId(id)) {
            throw new ConflitoException("Não é possível excluir um paciente que possui agendamentos");
        }
        repository.delete(p);
    }

    /** Usado internamente por outros serviços. */
    public Paciente obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado: id " + id));
    }
}
