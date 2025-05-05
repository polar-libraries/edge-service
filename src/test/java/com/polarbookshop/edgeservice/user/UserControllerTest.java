package com.polarbookshop.edgeservice.user;

import com.polarbookshop.edgeservice.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void wheNotAuthenticatedThen401(){

        webClient.get()
                .uri("/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

}