package br.com.clinica.dto;

import br.com.clinica.model.Agendamento;
import br.com.clinica.model.StatusAgendamento;

import java.time.LocalDateTime;

public record AgendamentoResponse(Long id,
                                  Long pacienteId, String pacienteNome,
                                  Long profissionalId, String profissionalNome, String especialidade,
                                  LocalDateTime dataHora, StatusAgendamento status, String observacoes) {

    public static AgendamentoResponse de(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(),
                a.getPaciente().getId(), a.getPaciente().getNome(),
                a.getProfissional().getId(), a.getProfissional().getNome(), a.getProfissional().getEspecialidade(),
                a.getDataHora(), a.getStatus(), a.getObservacoes());
    }
}
