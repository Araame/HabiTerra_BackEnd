package com.habiterra.shared.security;
import com.habiterra.identity.service.*;
import com.habiterra.shared.exception.ApiErrorWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.time.Clock;
import java.util.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean Clock clock(){return Clock.systemUTC();}
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
    @Bean AuthenticationManager authenticationManager(CustomUserDetailsService users,PasswordEncoder encoder){
        var provider=new DaoAuthenticationProvider(users);provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,JwtService tokens,AuthService auth,
            ApiErrorWriter errors,AuthRateLimiter limiter,CorsConfigurationSource cors)throws Exception {
        // Tokens are accepted exclusively in Authorization, never in cookies.
        http.csrf(c->c.disable()).cors(c->c.configurationSource(cors))
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(c->c.disable()).formLogin(c->c.disable()).httpBasic(c->c.disable()).logout(c->c.disable())
            .authorizeHttpRequests(a->a.requestMatchers(HttpMethod.POST,
                "/api/v1/auth/request-otp","/api/v1/auth/resend-otp","/api/v1/auth/verify-otp",
                "/api/v1/auth/complete-registration","/api/v1/auth/login").permitAll().anyRequest().authenticated())
            .exceptionHandling(e->e.authenticationEntryPoint((q,s,x)->errors.write(q,s,401,"UNAUTHORIZED","Authentification requise"))
                .accessDeniedHandler((q,s,x)->errors.write(q,s,403,"ACCESS_DENIED","Acces interdit")))
            .addFilterBefore(new JwtAuthenticationFilter(tokens,auth,errors,limiter),UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${auth.cors.origins:}") String origins){
        CorsConfiguration c=new CorsConfiguration();
        c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::strip).filter(s->!s.isEmpty()).toList());
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization","Content-Type"));c.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",c);return source;
    }
}
