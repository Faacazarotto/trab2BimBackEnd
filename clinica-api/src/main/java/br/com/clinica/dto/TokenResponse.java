package br.com.clinica.dto;

public record TokenResponse(String token, String tipo, String login, String perfil) {
}
