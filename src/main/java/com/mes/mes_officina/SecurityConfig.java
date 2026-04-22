package com.mes.mes_officina;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;

@Configuration
public class SecurityConfig {

    @Value("${app.security.password}")
    private String appPassword;

    @Autowired
    private JdbcTokenRepositoryImpl tokenRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JdbcTokenRepositoryImpl tokenRepository) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/login.html",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/index.html", false)
                        .permitAll()
                )

                // 🔥 REMEMBER-ME VERO
                .rememberMe(remember -> remember
                        .key("MES_SUPER_STABLE_KEY_2026")
                        .tokenValiditySeconds(60 * 60 * 24 * 90) // 90 giorni
                        .alwaysRemember(true)
                        .useSecureCookie(true) // 🔥 fondamentale su Render (HTTPS)
                        .tokenRepository(tokenRepository)
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login.html")
                        .deleteCookies("JSESSIONID", "remember-me")
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            if (!username.equals("admin")) {
                throw new UsernameNotFoundException("User not found");
            }

            return User.withUsername("admin")
                    .password(passwordEncoder().encode(appPassword))
                    .roles("ADMIN")
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}