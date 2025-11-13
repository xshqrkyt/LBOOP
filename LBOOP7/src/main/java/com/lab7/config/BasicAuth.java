package com.lab7.config;

import com.lab7.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
                        .requestMatchers("/lab6-1.0-SNAPSHOT/**").hasRole("ADMIN")
                        // Пользователи с ролью USER могут только GET-запросы на указанных ресурсах
                        .requestMatchers(HttpMethod.GET, "/lab6-1.0-SNAPSHOT/users/**",
                                "/lab6-1.0-SNAPSHOT/points/**",
                                "/lab6-1.0-SNAPSHOT/functions/**",
                                "/lab6-1.0-SNAPSHOT/composite-functions/**",
                                "/lab6-1.0-SNAPSHOT/composite-function-links/**").hasRole("USER")
                        // Для остальных HTTP методов на этих же ресурсах - доступ запрещён
                        .requestMatchers("/lab6-1.0-SNAPSHOT/users/**",
                                "/lab6-1.0-SNAPSHOT/points/**",
                                "/lab6-1.0-SNAPSHOT/functions/**",
                                "/lab6-1.0-SNAPSHOT/composite-functions/**",
                                "/lab6-1.0-SNAPSHOT/composite-function-links/**").denyAll()
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}