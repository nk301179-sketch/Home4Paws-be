package com.ooad.home4paws.controller;

import com.ooad.home4paws.dto.LoginRequest;
import com.ooad.home4paws.dto.RegisterRequest;
import com.ooad.home4paws.entity.User;
import com.ooad.home4paws.security.JwtUtils;
import com.ooad.home4paws.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "Register a new user", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User successfully registered"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest req) {
        User saved = userService.registerNewUser(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered: " + saved.getUsername());
    }

    @Operation(summary = "Authenticate user", description = "Login with username and password to receive JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        List<String> roles = principal.getAuthorities()
                                      .stream()
                                      .map(a -> a.getAuthority())
                                      .collect(Collectors.toList());

        String jwt = jwtUtils.generateToken(principal.getUsername(), roles);

        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "username", principal.getUsername(),
                "roles", roles
        ));
    }
}
