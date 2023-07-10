package wf.garnier.springone.authserverk8s.authserverk8s;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
class SecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
        http.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));
        return http.build();
    }


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
                .formLogin(Customizer.withDefaults())
                .build();
    }


    @Bean
    JWKSource<SecurityContext> jwkSource() throws Exception {
        return new KubernetesJwkRepository();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new InMemoryRegisteredClientRepository(
                RegisteredClient.withId("test-client")
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .clientId("test-client")
                        .clientSecret("{noop}test-secret")
                        .redirectUri("http://client.127.0.0.1.nip.io/login/oauth2/code/login-client")
                        .redirectUri("http://token-viewer.127.0.0.1.nip.io/oauth2/callback")
                        .scope("openid")
                        .scope("profile")
                        .scope("email")
                        .build()
        );
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("daniel")
                        .password("{noop}password")
                        .roles("user")
                        .build()
        );
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> customizer() {
        return context -> {
            context.getClaims().claim(StandardClaimNames.EMAIL, context.getPrincipal().getName() + "@example.com");
            context.getClaims().claim(StandardClaimNames.EMAIL_VERIFIED, true   );
            context.getClaims().claim(StandardClaimNames.PREFERRED_USERNAME, context.getPrincipal().getName());
            var groups = context.getPrincipal().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            context.getClaims().claim("groups", groups);
        };
    }
}
