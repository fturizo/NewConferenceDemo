package com.fturizo.demos.session;

import com.fturizo.demos.session.util.OidcClient;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    private static final String CLIENT_ID = "it";
    private static final String CLIENT_SECRET = "UG9IZIzk18xCt0Uq3cXivSP1VpA1cedT";

    @Bean
    @Profile("secured")
    public OidcClient oidcClient(OAuth2ResourceServerProperties rsProperties){
        return new OidcClient(rsProperties, new RestTemplate(), CLIENT_ID, CLIENT_SECRET);
    }
}
