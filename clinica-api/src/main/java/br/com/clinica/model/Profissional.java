package br.com.clinica.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profissionais")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 80)
    private String especialidade;

    @Column(nullable = false, unique = true, length = 30)
    private String registro; // ex.: CRM-PR 12345

    @Column(nullable = false, length = 120)
    private String email;

    // 1:N - um profissional atende vários agendamentos
    @OneToMany(mappedBy = "profissional")
    private List<Agendamento> agendamentos = new ArrayList<>();

    protected Profissional() {
    }

    public Profissional(String nome, String especialidade, String registro, String email) {
        this.nome = nome;
        this.especialidade = especialidade;
        this.registro = registro;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEspecialidade() { return especialidade; }
    public String getRegistro() { return registro; }
    public String getEmail() { return email; }
    public List<Agendamento> getAgendamentos() { return agendamentos; }

    public void setNome(String nome) { this.nome = nome; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public void setRegistro(String registro) { this.registro = registro; }
    public void setEmail(String email) { this.email = email; }
}
