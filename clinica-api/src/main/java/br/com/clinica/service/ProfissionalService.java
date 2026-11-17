package br.com.clinica.service;

import br.com.clinica.dto.ProfissionalRequest;
import br.com.clinica.dto.ProfissionalResponse;
import br.com.clinica.exception.ConflitoException;
import br.com.clinica.exception.RecursoNaoEncontradoException;
import br.com.clinica.model.Profissional;
import br.com.clinica.repository.AgendamentoRepository;
import br.com.clinica.repository.ProfissionalRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfissionalService {

    private final ProfissionalRepository repository;
    private final AgendamentoRepository agendamentoRepository;

    public ProfissionalService(ProfissionalRepository repository, AgendamentoRepository agendamentoRepository) {
        this.repository = repository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional
    public ProfissionalResponse criar(ProfissionalRequest r) {
        if (repository.existsByRegistro(r.registro())) {
            throw new ConflitoException("Já existe um profissional com este registro");
        }
        Profissional p = new Profissional(r.nome(), r.especialidade(), r.registro(), r.email());
        return ProfissionalResponse.de(repository.save(p));
    }

    /** Consulta com múltiplos parâmetros opcionais: nome (parcial) e especialidade (parcial). */
    @Transactional(readOnly = true)
    public List<ProfissionalResponse> listar(String nome, String especialidade) {
        Specification<Profissional> spec = (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%"));
        }
        if (especialidade != null && !especialidade.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("especialidade")), "%" + especialidade.trim().toLowerCase() + "%"));
        }

        return repository.findAll(spec, Sort.by("nome")).stream().map(ProfissionalResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProfissionalResponse buscar(Long id) {
        return ProfissionalResponse.de(obter(id));
    }

    @Transactional
    public ProfissionalResponse atualizar(Long id, ProfissionalRequest r) {
        Profissional p = obter(id);
        if (repository.existsByRegistroAndIdNot(r.registro(), id)) {
            throw new ConflitoException("Já existe outro profissional com este registro");
        }
        p.setNome(r.nome());
        p.setEspecialidade(r.especialidade());
        p.setRegistro(r.registro());
        p.setEmail(r.email());
        return ProfissionalResponse.de(repository.save(p));
    }

    @Transactional
    public void excluir(Long id) {
        Profissional p = obter(id);
        if (agendamentoRepository.existsByProfissionalId(id)) {
            throw new ConflitoException("Não é possível excluir um profissional que possui agendamentos");
        }
        repository.delete(p);
    }

    public Profissional obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado: id " + id));
    }
}
