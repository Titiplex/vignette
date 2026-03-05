package org.titiplex.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.titiplex.api.dto.*;
import org.titiplex.persistence.model.User;
import org.titiplex.service.UserService;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Endpoint", description = "Endpoints for user authentication and registration.")
public class AuthApiController {

    private final AuthenticationManager authManager;
    private final JwtEncoder jwtEncoder;
    private final UserService users;

    public AuthApiController(AuthenticationManager authManager, JwtEncoder jwtEncoder, UserService users) {
        this.authManager = authManager;
        this.jwtEncoder = jwtEncoder;
        this.users = users;
    }

    /**
     * Authenticates a user based on credentials provided in the request body.
     * If authentication is successful, generates a JWT token for the user along with session handling.
     *
     * @param req     the {@link LoginRequest} containing the username and password
     * @param request the HTTP servlet request, used for managing session and security context
     * @return a {@link LoginResponse} containing the generated JWT token and its expiration time
     */
    @Operation(
            summary = "Authenticate a user.",
            description = "Authenticates a user and generate a JWT token for the session."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User authenticated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials : username or password incorrect"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing or malformed request body"
            )
    })
    @PostMapping("/login")
    public LoginResponse login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Username and password to authenticate",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class)
                    )
            )
            @RequestBody LoginRequest req,
            @Parameter(hidden = true)
            HttpServletRequest request) {

        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        if (request.getSession() != null) request.changeSessionId();

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

    /**
     * Logs out the currently authenticated user by invalidating their session and clearing the security context.
     *
     * @param request the HTTP servlet request, used to retrieve and invalidate the session
     */
    @Operation(
            summary = "Logout the current user.",
            description = "Logs out the currently authenticated user by invalidating their session and clearing the security context."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User logged out successfully"
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "User logged out successfully"
            )
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Parameter(hidden = true) HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
    }

    /**
     * Retrieves information about the currently authenticated user.
     * If the authentication is null, returns null.
     * If the user cannot be found, throws an IllegalStateException.
     *
     * @param auth the authentication object representing the currently authenticated user
     * @return a {@link MeResponse} containing the user's ID, username, and roles
     */
    @Operation(
            summary = "Get authenticated user self information.",
            description = "Retrieves information about the currently authenticated user." +
                    "If the authentication is null, returns null." +
                    "If the user cannot be found, throws an IllegalStateException."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Information fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MeResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Authenticated user not found in database"
            )
    })
    @GetMapping("/me")
    public MeResponse me(@Parameter(hidden = true) Authentication auth) {
        if (auth == null) return null;
        User u = users.getUserByUsername(auth.getName());
        if (u == null) throw new IllegalStateException("user not found");
        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return new MeResponse(u.getId(), u.getUsername(), roles);
    }

    /**
     * Registers a new user with the provided details.
     * Ensures that valid data is provided and checks if the username or email is already in use.
     * If the registration is successful, creates a new user and returns the response containing the user's ID and username.
     *
     * @param req the {@link RegisterRequest} containing the username, email, and password
     * @return a {@link RegisterResponse} containing the ID and username of the newly registered user
     * @throws IllegalArgumentException if any input validation fails (e.g., missing or invalid fields, or duplicate username/email)
     */
    @Operation(
            summary = "Registers a new user.",
            description = "Registers a new user with the provided details." +
                    "Ensures that valid data is provided and checks if the username or email is already in use." +
                    "If the registration is successful, creates a new user and returns the response containing the user's ID and username."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Registered successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input : missing required fields, password too short, or malformed data"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict : username or email already exists"
            )
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class)
                    )
            )
            @RequestBody RegisterRequest req
    ) {
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
