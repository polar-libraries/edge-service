package com.polarbookshop.edgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ReactiveClientRegistrationRepository clientRegistrationRepository) {

        return http
                .authorizeExchange(exchange -> exchange
                        // Rotas públicas
                        .pathMatchers("/", "/favicon.ico", "/assets/**", "/css/**", "/js/**", "/*.css", "/*.js")
                        .permitAll()
                        .pathMatchers(HttpMethod.GET, "/books/**").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exception -> exception
                        // Força 401 sem redirecionar para login
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
                )
                .csrf(csrfSpec -> csrfSpec.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(getServerCsrfTokenRequestAttributeHandler()))
                .build();
    }

    private static ServerCsrfTokenRequestAttributeHandler getServerCsrfTokenRequestAttributeHandler() {
        ServerCsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new ServerCsrfTokenRequestAttributeHandler();
        csrfTokenRequestAttributeHandler.setTokenFromMultipartDataEnabled(true);
        return csrfTokenRequestAttributeHandler;
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        var logoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return logoutSuccessHandler;
    }

    @Bean
    public WebFilter csrfWebFilter() {
        return ((exchange, chain) -> {
            exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
                Mono<CsrfToken> csrfToken =
                        exchange.getAttribute(CsrfToken.class.getName());
                return csrfToken != null ? csrfToken.then() : Mono.empty();
            }));
            return chain.filter(exchange);
        });
    }

}
