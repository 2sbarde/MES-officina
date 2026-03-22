package com.mes.mes_officina;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserConfig {

    @Bean
    public UserDetailsService users() {

        UserDetails ufficio = User.builder()
                .username("ufficio")
                .password("{noop}jennybellagnocca")
                .roles("UFF")
                .build();

        UserDetails officina = User.builder()
                .username("officina")
                .password("{noop}jennybellagnocca")
                .roles("OFF")
                .build();

        return new InMemoryUserDetailsManager(ufficio, officina);
    }
}
