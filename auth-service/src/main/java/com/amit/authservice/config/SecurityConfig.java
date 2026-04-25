package com.amit.authservice.config;

import com.amit.authservice.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())

            // 🔥 IMPORTANT (CORS enable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/owner/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/agent/**").hasAnyRole("AGENT", "ADMIN")

                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🔹 CORS Configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}