package br.com.clinica.service;

import br.com.clinica.dto.AgendamentoRequest;
import br.com.clinica.dto.AgendamentoResponse;
import br.com.clinica.exception.ConflitoException;
import br.com.clinica.exception.RecursoNaoEncontradoException;
import br.com.clinica.exception.RegraNegocioException;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Profissional;
import br.com.clinica.model.StatusAgendamento;
import br.com.clinica.repository.AgendamentoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AgendamentoService {

    private static final LocalTime ABERTURA = LocalTime.of(7, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(19, 0);
    private static final Long SEM_IGNORAR = -1L;

    private final AgendamentoRepository repository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    public AgendamentoService(AgendamentoRepository repository,
                              PacienteService pacienteService,
                              ProfissionalService profissionalService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
    }

    @Transactional
    public AgendamentoResponse criar(AgendamentoRequest r) {
        Paciente paciente = pacienteService.obter(r.pacienteId());
        Profissional profissional = profissionalService.obter(r.profissionalId());

        validarHorario(r.dataHora());
        validarConflitos(paciente.getId(), profissional.getId(), r.dataHora(), SEM_IGNORAR);

        Agendamento a = new Agendamento(paciente, profissional, r.dataHora(), r.observacoes());
        return AgendamentoResponse.de(repository.save(a));
    }

    /**
     * Consulta com múltiplos parâmetros opcionais:
     * pacienteId, profissionalId, status e intervalo de datas (de / ate, inclusivos).
     */
    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listar(Long pacienteId, Long profissionalId, StatusAgendamento status,
                                            LocalDate de, LocalDate ate) {
        Specification<Agendamento> spec = (root, query, cb) -> cb.conjunction();

        if (pacienteId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paciente").get("id"), pacienteId));
        }
        if (profissionalId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("profissional").get("id"), profissionalId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (de != null) {
            LocalDateTime inicio = de.atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDateTime>get("dataHora"), inicio));
        }
        if (ate != null) {
            LocalDateTime fim = ate.atTime(LocalTime.MAX);
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.<LocalDateTime>get("dataHora"), fim));
        }

        return repository.findAll(spec, Sort.by("dataHora")).stream().map(AgendamentoResponse::de).toList();
    }

    /** Consulta com filtro no path: GET /pacientes/{id}/agendamentos */
    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listarPorPaciente(Long pacienteId) {
        pacienteService.obter(pacienteId); // garante 404 se o paciente não existir
        return repository.findByPacienteIdOrderByDataHora(pacienteId).stream().map(AgendamentoResponse::de).toList();
    }

    /** Consulta com filtro no path: GET /profissionais/{id}/agendamentos */
    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listarPorProfissional(Long profissionalId) {
        profissionalService.obter(profissionalId);
        return repository.findByProfissionalIdOrderByDataHora(profissionalId).stream().map(AgendamentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse buscar(Long id) {
        return AgendamentoResponse.de(obter(id));
    }

    @Transactional
    public AgendamentoResponse atualizar(Long id, AgendamentoRequest r) {
        Agendamento a = obter(id);
        if (a.getStatus() != StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException("Somente agendamentos com status AGENDADO podem ser alterados");
        }

        Paciente paciente = pacienteService.obter(r.pacienteId());
        Profissional profissional = profissionalService.obter(r.profissionalId());

        validarHorario(r.dataHora());
        validarConflitos(paciente.getId(), profissional.getId(), r.dataHora(), a.getId());

        a.setPaciente(paciente);
        a.setProfissional(profissional);
        a.setDataHora(r.dataHora());
        a.setObservacoes(r.observacoes());
        return AgendamentoResponse.de(repository.save(a));
    }

    /** Transições permitidas: AGENDADO -> CONCLUIDO ou AGENDADO -> CANCELADO. */
    @Transactional
    public AgendamentoResponse alterarStatus(Long id, StatusAgendamento novoStatus) {
        Agendamento a = obter(id);

        if (a.getStatus() != StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException("O agendamento já está " + a.getStatus() + " e não pode mudar de status");
        }
        if (novoStatus == StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException("Transição inválida: informe CONCLUIDO ou CANCELADO");
        }
        if (novoStatus == StatusAgendamento.CONCLUIDO && a.getDataHora().isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível concluir um atendimento que ainda não aconteceu");
        }

        a.setStatus(novoStatus);
        return AgendamentoResponse.de(repository.save(a));
    }

    @Transactional
    public void excluir(Long id) {
        Agendamento a = obter(id);
        if (a.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new RegraNegocioException("Agendamentos concluídos fazem parte do histórico e não podem ser excluídos");
        }
        repository.delete(a);
    }

    // ---------- regras de negócio auxiliares ----------

    private Agendamento obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado: id " + id));
    }

    private void validarHorario(LocalDateTime dataHora) {
        if (!dataHora.isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("O agendamento deve ser feito para uma data e hora futuras");
        }
        LocalTime hora = dataHora.toLocalTime();
        if (hora.isBefore(ABERTURA) || !hora.isBefore(FECHAMENTO)) {
            throw new RegraNegocioException("A clínica atende apenas entre 07:00 e 19:00");
        }
    }

    private void validarConflitos(Long pacienteId, Long profissionalId, LocalDateTime dataHora, Long ignorarId) {
        if (repository.existeConflitoProfissional(profissionalId, dataHora, ignorarId)) {
            throw new ConflitoException("O profissional já possui um agendamento neste horário");
        }
        if (repository.existeConflitoPaciente(pacienteId, dataHora, ignorarId)) {
            throw new ConflitoException("O paciente já possui um agendamento neste horário");
        }
    }
}
