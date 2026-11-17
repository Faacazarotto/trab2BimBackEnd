package br.com.clinica.repository;

import br.com.clinica.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>, JpaSpecificationExecutor<Agendamento> {

    List<Agendamento> findByPacienteIdOrderByDataHora(Long pacienteId);

    List<Agendamento> findByProfissionalIdOrderByDataHora(Long profissionalId);

    boolean existsByPacienteId(Long pacienteId);

    boolean existsByProfissionalId(Long profissionalId);

    /** Verifica se o profissional já tem um agendamento não cancelado no mesmo horário. */
    @Query("""
            select count(a) > 0 from Agendamento a
            where a.profissional.id = :profissionalId
              and a.dataHora = :dataHora
              and a.status <> br.com.clinica.model.StatusAgendamento.CANCELADO
              and a.id <> :ignorarId
            """)
    boolean existeConflitoProfissional(@Param("profissionalId") Long profissionalId,
                                       @Param("dataHora") LocalDateTime dataHora,
                                       @Param("ignorarId") Long ignorarId);

    /** Verifica se o paciente já tem um agendamento não cancelado no mesmo horário. */
    @Query("""
            select count(a) > 0 from Agendamento a
            where a.paciente.id = :pacienteId
              and a.dataHora = :dataHora
              and a.status <> br.com.clinica.model.StatusAgendamento.CANCELADO
              and a.id <> :ignorarId
            """)
    boolean existeConflitoPaciente(@Param("pacienteId") Long pacienteId,
                                   @Param("dataHora") LocalDateTime dataHora,
                                   @Param("ignorarId") Long ignorarId);
}
