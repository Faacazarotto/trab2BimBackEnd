package br.com.clinica.dto;

import br.com.clinica.model.Profissional;

public record ProfissionalResponse(Long id, String nome, String especialidade, String registro, String email) {

    public static ProfissionalResponse de(Profissional p) {
        return new ProfissionalResponse(p.getId(), p.getNome(), p.getEspecialidade(), p.getRegistro(), p.getEmail());
    }
}
