package com.habiterra.shared.security;

import com.habiterra.identity.service.AuthService;
import com.habiterra.identity.service.CustomUserDetailsService;
import com.habiterra.shared.exception.ApiErrorWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    AuthenticationManager authenticationManager(
            CustomUserDetailsService users,
            PasswordEncoder encoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);

        return new ProviderManager(provider);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService tokens,
            AuthService auth,
            ApiErrorWriter errors,
            AuthRateLimiter limiter,
            @Qualifier("corsConfigurationSource")
            CorsConfigurationSource cors
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(corsConfig ->
                        corsConfig.configurationSource(cors)
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .requestCache(cache -> cache.disable())

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                .logout(logout -> logout.disable())

                .authorizeHttpRequests(authz -> authz

                        // Swagger
                        .requestMatchers(
                                HttpMethod.GET,
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()

                        // Routes publiques d'authentification
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/request-otp",
                                "/api/v1/auth/resend-otp",
                                "/api/v1/auth/verify-otp",
                                "/api/v1/auth/complete-registration",
                                "/api/v1/auth/login"
                        ).permitAll()

                        // Toutes les autres routes nécessitent un JWT
                        .anyRequest()
                        .authenticated()
                )

                .exceptionHandling(exceptions -> exceptions

                        .authenticationEntryPoint(
                                (request, response, exception) ->
                                        errors.write(
                                                request,
                                                response,
                                                401,
                                                "UNAUTHORIZED",
                                                "Authentification requise"
                                        )
                        )

                        .accessDeniedHandler(
                                (request, response, exception) ->
                                        errors.write(
                                                request,
                                                response,
                                                403,
                                                "ACCESS_DENIED",
                                                "Accès interdit"
                                        )
                        )
                )

                .addFilterBefore(
                        new JwtAuthenticationFilter(
                                tokens,
                                auth,
                                errors,
                                limiter
                        ),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${auth.cors.origins:}") String origins
    ) {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                Arrays.stream(origins.split(","))
                        .map(String::strip)
                        .filter(origin -> !origin.isEmpty())
                        .toList()
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}