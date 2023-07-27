package wf.garnier.springone.ssoclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> {
                    authorize.anyRequest().authenticated();
                })
                .oauth2Login(withDefaults())
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(InMemoryClientRegistrationRepository repository) {
        var clientReg = repository.iterator().next();
        return NimbusJwtDecoder.withIssuerLocation(clientReg.getProviderDetails().getIssuerUri()).build();
    }
}
