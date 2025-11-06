package com.lab6.config;

import com.lab6.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class BasicAuth {
    private final CustomUserDetailsService userDetailsService;

    public BasicAuth(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // Для админов полный доступ
                        .requestMatchers("/spring/**").hasRole("ADMIN")
                        // Пользователи с ролью USER могут только GET-запросы на указанных ресурсах
                        .requestMatchers(HttpMethod.GET, "/spring/users/**",
                                "/spring/points/**",
                                "/spring/functions/**",
                                "/spring/composite-functions/**",
                                "/spring/composite-function-links/**").hasRole("USER")
                        // Для остальных HTTP методов на этих же ресурсах - доступ запрещён
                        .requestMatchers("/spring/users/**",
                                "/spring/points/**",
                                "/spring/functions/**",
                                "/spring/composite-functions/**",
                                "/spring/composite-function-links/**").denyAll()
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}