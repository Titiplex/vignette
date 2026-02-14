package org.titiplex.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.titiplex.persistence.model.User;
import org.titiplex.service.UserService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authManager;
    private final JwtEncoder jwtEncoder;
    private final UserService users;

    public AuthApiController(AuthenticationManager authManager, JwtEncoder jwtEncoder, UserService users) {
        this.authManager = authManager;
        this.jwtEncoder = jwtEncoder;
        this.users = users;
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String accessToken, long expiresInSeconds) {
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req, HttpServletRequest request) {

        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        request.changeSessionId();

        // 1) session cookie (JSESSIONID)
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        // 2) JWT
        Instant now = Instant.now();
        long expires = 60 * 60; // 1h

        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expires))
                .subject(auth.getName())
                .claim("roles", roles)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new LoginResponse(token, expires);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
    }

    public record MeResponse(Long id, String username, List<String> roles) {
    }

    @GetMapping("/me")
    public MeResponse me(Authentication auth) {
        User u = users.getUserByUsername(auth.getName());
        if (u == null) throw new IllegalStateException("user not found");
        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return new MeResponse(u.getId(), u.getUsername(), roles);
    }

    public record RegisterRequest(String username, String email, String password) {
    }

    public record RegisterResponse(Long id, String username) {
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@RequestBody RegisterRequest req) {
        if (req.username() == null || req.username().isBlank()) throw new IllegalArgumentException("username required");
        if (req.email() == null || req.email().isBlank()) throw new IllegalArgumentException("email required");
        if (req.password() == null || req.password().length() < 8)
            throw new IllegalArgumentException("password too short");

        if (users.existsByUsername(req.username())) throw new IllegalArgumentException("username already used");
        if (users.existsByEmail(req.email())) throw new IllegalArgumentException("email already used");

        var username = req.username().trim();
        var email = req.email().trim();
        var pwd = req.password();

        User u = users.register(username, email, pwd);
        return new RegisterResponse(u.getId(), u.getUsername());
    }
}
