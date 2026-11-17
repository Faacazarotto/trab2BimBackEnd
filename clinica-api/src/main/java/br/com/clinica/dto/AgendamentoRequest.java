package br.com.clinica.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AgendamentoRequest(
        @NotNull(message = "O paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "O profissional é obrigatório")
        Long profissionalId,

        @NotNull(message = "A data e hora são obrigatórias")
        LocalDateTime dataHora,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes) {
}
