package br.com.clinica.repository;

import br.com.clinica.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long>, JpaSpecificationExecutor<Profissional> {
    boolean existsByRegistro(String registro);

    boolean existsByRegistroAndIdNot(String registro, Long id);
}
