package wf.garnier.spring.authserverk8s;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(
                        authorize -> {
                            authorize.requestMatchers("/").permitAll();
                            authorize.requestMatchers("/error").permitAll();
                            authorize.requestMatchers("/favicon.io").permitAll();
                            authorize.anyRequest().authenticated();
                        }
                )
                .build();
    }
}
