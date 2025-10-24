package com.ooad.home4paws.config;

import com.ooad.home4paws.security.CustomUserDetailsService;
import com.ooad.home4paws.security.JwtAuthFilter;
import com.ooad.home4paws.security.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtUtils jwtUtils) {
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Password encoder bean for hashing passwords.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT authentication filter bean.
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtUtils, userDetailsService);
    }

    /**
     * AuthenticationManager bean for handling authentication.
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    /**
     * CORS configuration - allows all origins, methods, and headers.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5174",
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8080",
                "https://test-liart-two-87.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Main security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS and disable CSRF
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session management
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/login").permitAll()  // Admin login endpoint
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Public read-only access for guests
                .requestMatchers("/api/surrender-dogs").permitAll()  // View all dogs (adopt page)
                .requestMatchers("/api/surrender-dogs/{id}").permitAll()  // View single dog
                .requestMatchers("/api/dogs").permitAll()  // View all dogs
                .requestMatchers("/api/dogs/{id}").permitAll()  // View single dog
                .requestMatchers("/api/dogs/adopt").permitAll()  // View dogs for adoption
                .requestMatchers("/api/dogs/buy").permitAll()  // View dogs for sale
                .requestMatchers("/api/dogs/status/{status}").permitAll()  // View dogs by status
                .requestMatchers("/api/reports").permitAll()  // Returns empty for guests
                
                // Contact message submission - allow public access
                .requestMatchers("/api/contact-messages").permitAll()
                
                // Application endpoints - require authentication
                .requestMatchers("/api/applications/**").authenticated()
                
                // Contact message management - require authentication
                .requestMatchers("/api/contact-messages/my-messages").authenticated()
                .requestMatchers("/api/contact-messages/{id}").authenticated()
                
                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Protected endpoints - require authentication
                .requestMatchers("/api/reports/my-reports").authenticated()  // User's own reports
                .requestMatchers("/api/surrender-dogs/my-requests").authenticated()  // User's own surrender requests
                
                // All other endpoints require authentication (POST, PUT, DELETE)
                .anyRequest().authenticated()
            )

            // Add JWT authentication filter
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
