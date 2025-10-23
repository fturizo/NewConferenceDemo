package com.fturizo.demos.session;

import com.fturizo.demos.session.entities.Session;
import com.fturizo.demos.session.repositories.SessionRepository;
import com.fturizo.demos.session.util.OidcClient;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("secured")
class SessionApplicationTests {

    private static final String REALM_NAME = "conference";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"));

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.4")
            .withBootstrapAdminDisabled()
            .withRealmImportFile("conference-realm.json");

    @DynamicPropertySource
    static void configureKeycloakOIDC(DynamicPropertyRegistry registry){
        keycloak.start();
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "%s/realms/%s".formatted(keycloak.getAuthServerUrl(), REALM_NAME));
        registry.add("spring.security.oauth2.resourceserver.jwt.public-key-location",
                () -> "%s/realms/%s/protocol/openid-connect/certs".formatted(keycloak.getAuthServerUrl(), REALM_NAME));
    }

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OidcClient oidcClient;

    @BeforeEach
    public void clearSessions(){
        sessionRepository.deleteAll();
    }

    @Test
    public void add_sessions_by_repo(){
        sessionRepository.saveAll(List.of(
                new Session("Test Session 1", "OCARINA", LocalDate.now().plusDays(1), Duration.ofHours(1), List.of("Speaker 1")),
                new Session("Test Session 2", "ESPERANZA", LocalDate.now().plusDays(7), Duration.ofMinutes(45), List.of("Speaker 1"))
        ));
        var org1speakers = sessionRepository.findAll();
        assertThat(org1speakers.size()).isEqualTo(2);
    }

    @Test
    public void test_unauthenticated(){
        var response = restTemplate.getForEntity("/session/{id}", Session.class, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void test_authenticated(){
        var session = new Session("Test Session", "OCARINA", LocalDate.now().plusDays(1), Duration.ofHours(1), List.of("Speaker 1"));
        sessionRepository.save(session);

        var headers = prepareAuthHeader("malfa", "malfa");
        var response = restTemplate.exchange("/session/{id}", HttpMethod.GET, new HttpEntity<>(headers), Session.class, session.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo(session.getTitle());
        assertThat(response.getBody().getDuration()).isEqualTo(session.getDuration());
        assertThat(response.getBody().getVenue()).isEqualTo(session.getVenue());
    }

    @Test
    public void admin_should_create_sessions(){
        var session = new Session("Test Session", "OCARINA", LocalDate.now().plusDays(1), Duration.ofHours(1), List.of("Speaker 1"));
        var headers = prepareAuthHeader("malfa", "malfa");
        var response = restTemplate.exchange("/session", HttpMethod.POST, new HttpEntity<>(session, headers), Void.class, session.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        var currentSessions = sessionRepository.findAll();
        assertThat(currentSessions).hasSize(1);
        assertThat(currentSessions.getFirst().getTitle()).isEqualTo(session.getTitle());
        assertThat(currentSessions.getFirst().getDate()).isEqualTo(session.getDate());
    }

    @Test
    public void speaker_should_not_create_sessions(){
        var session = new Session("Test Session", "OCARINA", LocalDate.now().plusDays(1), Duration.ofHours(1), List.of("Speaker 1"));

        var headers = prepareAuthHeader("cbeta", "cbeta");
        var response = restTemplate.exchange("/session", HttpMethod.POST, new HttpEntity<>(session, headers), Session.class, session.getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private HttpHeaders prepareAuthHeader(String username, String password){
        var tokenInfo = oidcClient.getToken(username, password);
        var headers = new HttpHeaders();
        headers.put("Authorization", List.of("Bearer %s".formatted(tokenInfo.accessToken())));
        return headers;
    }
}
