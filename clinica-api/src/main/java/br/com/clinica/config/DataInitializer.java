package br.com.clinica.config;

import br.com.clinica.model.Paciente;
import br.com.clinica.model.Perfil;
import br.com.clinica.model.Profissional;
import br.com.clinica.model.Usuario;
import br.com.clinica.repository.PacienteRepository;
import br.com.clinica.repository.ProfissionalRepository;
import br.com.clinica.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

/** Carga inicial: usuários de teste e alguns dados de exemplo (só se o banco estiver vazio). */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner carregarDados(UsuarioRepository usuarios, PacienteRepository pacientes,
                                    ProfissionalRepository profissionais, PasswordEncoder encoder) {
        return args -> {
            if (!usuarios.existsByLogin("admin")) {
                usuarios.save(new Usuario("admin", encoder.encode("admin123"), Perfil.ADMIN));
            }
            if (!usuarios.existsByLogin("comum")) {
                usuarios.save(new Usuario("comum", encoder.encode("comum123"), Perfil.COMUM));
            }
            if (pacientes.count() == 0) {
                pacientes.save(new Paciente("Maria Silva", "12345678901", "maria@email.com",
                        "(45) 99999-0001", LocalDate.of(1990, 5, 20)));
                pacientes.save(new Paciente("João Souza", "98765432100", "joao@email.com",
                        "(45) 99999-0002", LocalDate.of(1985, 11, 3)));
            }
            if (profissionais.count() == 0) {
                profissionais.save(new Profissional("Dra. Ana Lima", "Cardiologia", "CRM-PR 11111", "ana@clinica.com"));
                profissionais.save(new Profissional("Dr. Carlos Rocha", "Dermatologia", "CRM-PR 22222", "carlos@clinica.com"));
            }
        };
    }
}
