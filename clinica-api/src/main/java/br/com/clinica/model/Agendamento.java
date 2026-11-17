package br.com.clinica.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusAgendamento status;

    @Column(length = 500)
    private String observacoes;

    protected Agendamento() {
    }

    public Agendamento(Paciente paciente, Profissional profissional, LocalDateTime dataHora, String observacoes) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.observacoes = observacoes;
        this.status = StatusAgendamento.AGENDADO;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public Profissional getProfissional() { return profissional; }
    public LocalDateTime getDataHora() { return dataHora; }
    public StatusAgendamento getStatus() { return status; }
    public String getObservacoes() { return observacoes; }

    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public void setStatus(StatusAgendamento status) { this.status = status; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
