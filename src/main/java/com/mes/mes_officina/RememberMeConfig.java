package com.mes.mes_officina;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;

import javax.sql.DataSource;

@Configuration
public class RememberMeConfig {

    @Bean
    public JdbcTokenRepositoryImpl tokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);

        // ⚠️ SOLO LA PRIMA VOLTA (poi metti false)
        repo.setCreateTableOnStartup(true);

        return repo;
    }
}