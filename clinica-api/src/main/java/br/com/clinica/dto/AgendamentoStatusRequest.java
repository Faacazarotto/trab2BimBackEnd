package br.com.clinica.dto;

import br.com.clinica.model.StatusAgendamento;
import jakarta.validation.constraints.NotNull;

public record AgendamentoStatusRequest(
        @NotNull(message = "O status é obrigatório (CONCLUIDO ou CANCELADO)")
        StatusAgendamento status) {
}
