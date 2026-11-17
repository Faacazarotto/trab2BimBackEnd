package br.com.clinica.service;

import br.com.clinica.dto.LoginRequest;
import br.com.clinica.dto.TokenResponse;
import br.com.clinica.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest r) {
        // lança BadCredentialsException (-> 401) se login/senha estiverem errados
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(r.login(), r.senha()));

        UserDetails user = (UserDetails) auth.getPrincipal();
        String perfil = user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return new TokenResponse(jwtService.gerar(user), "Bearer", user.getUsername(), perfil);
    }
}
