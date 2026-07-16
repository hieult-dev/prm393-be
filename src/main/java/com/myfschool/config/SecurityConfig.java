package com.myfschool.config;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new LegacyAwarePasswordEncoder();
    }

    @Bean
    SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return NimbusJwtEncoder.withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey secretKey) {
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> roleAuthorities = claimAsList(jwt.getClaim("roles")).stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .map(GrantedAuthority.class::cast)
                    .toList();
            List<GrantedAuthority> permissionAuthorities = claimAsList(jwt.getClaim("permissions")).stream()
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();
            return Stream.concat(roleAuthorities.stream(), permissionAuthorities.stream()).toList();
        });
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Stream.concat(
                        Arrays.stream(allowedOrigins.split(",")),
                        Stream.of("http://localhost:*", "http://127.0.0.1:*"))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .distinct()
                .toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Firebase-ID-Token"));
        configuration.setExposedHeaders(Arrays.asList("WWW-Authenticate"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, HttpStatus.UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, HttpStatus.FORBIDDEN, "Forbidden"))
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/forgot-password/verify-phone",
                                "/api/auth/forgot-password/reset"
                        ).permitAll()
                        .requestMatchers("/api/auth/**", "/api/users/login").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/teacher/**").hasAnyRole("TEACHER", "SUBJECT_TEACHER", "HOMEROOM_TEACHER")
                        .requestMatchers("/api/parent/**").hasRole("PARENT")
                        .requestMatchers(HttpMethod.GET, "/api/profile", "/api/profile/**").authenticated()
                        .requestMatchers(
                                "/api/users", "/api/users/**",
                                "/api/roles", "/api/roles/**",
                                "/api/permissions", "/api/permissions/**",
                                "/api/password-reset-tokens", "/api/password-reset-tokens/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/student-grades/mark-report",
                                "/api/student-grades/*/mark-detail"
                        ).authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/student-grades/search").hasRole("ADMIN")
                        .requestMatchers("/api/student-grades", "/api/student-grades/**").denyAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/schedules/search",
                                "/api/schedules/day",
                                "/api/schedules/weekly"
                        ).authenticated()
                        .requestMatchers("/api/schedules", "/api/schedules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/attendance-reports/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/attendance-reports/search").hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/exam-schedules/me",
                                "/api/exam-schedules/search"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/exam-schedules",
                                "/api/exam-schedules/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/student-applications", "/api/student-applications/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/student-applications").denyAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/student-applications/*/review").hasRole("HOMEROOM_TEACHER")
                        .requestMatchers("/api/student-applications", "/api/student-applications/**").denyAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/application-types", "/api/application-types/**",
                                "/api/clubs", "/api/clubs/**",
                                "/api/club-members", "/api/club-members/**",
                                "/api/exam-schedules", "/api/exam-schedules/**",
                                "/api/events", "/api/events/**",
                                "/api/semesters", "/api/semesters/**",
                                "/api/semester-subjects", "/api/semester-subjects/**",
                                "/api/subjects", "/api/subjects/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/application-types", "/api/application-types/**",
                                "/api/clubs", "/api/clubs/**",
                                "/api/club-members", "/api/club-members/**",
                                "/api/exam-schedules", "/api/exam-schedules/**",
                                "/api/events", "/api/events/**",
                                "/api/semesters", "/api/semesters/**",
                                "/api/semester-subjects", "/api/semester-subjects/**",
                                "/api/subjects", "/api/subjects/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/application-types", "/api/application-types/**",
                                "/api/clubs", "/api/clubs/**",
                                "/api/club-members", "/api/club-members/**",
                                "/api/exam-schedules", "/api/exam-schedules/**",
                                "/api/events", "/api/events/**",
                                "/api/semesters", "/api/semesters/**",
                                "/api/semester-subjects", "/api/semester-subjects/**",
                                "/api/subjects", "/api/subjects/**"
                        ).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    private static List<String> claimAsList(Object claim) {
        if (claim instanceof Collection<?> values) {
            return values.stream()
                    .map(String::valueOf)
                    .toList();
        }
        return List.of();
    }

    private static void writeError(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"success":false,"message":"%s","data":null,"timestamp":"%s"}"""
                .formatted(escapeJson(message), LocalDateTime.now()));
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
