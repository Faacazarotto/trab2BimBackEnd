package br.com.clinica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfissionalRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "A especialidade é obrigatória")
        @Size(max = 80, message = "A especialidade deve ter no máximo 80 caracteres")
        String especialidade,

        @NotBlank(message = "O registro profissional é obrigatório")
        @Size(max = 30, message = "O registro deve ter no máximo 30 caracteres")
        String registro,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email) {
}
