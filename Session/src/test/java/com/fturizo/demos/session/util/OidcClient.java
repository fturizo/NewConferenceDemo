package com.fturizo.demos.session.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class OidcClient {

    //Only to be used for testing purposes!
    private static final String GRANT_TYPE = "password";

    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;
    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;

    public OidcClient(OAuth2ResourceServerProperties oAuth2ResourceServerProperties, RestTemplate restTemplate, String clientId, String clientSecret) {
        this.oAuth2ResourceServerProperties = oAuth2ResourceServerProperties;
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public TokenInfo getToken(String username, String password){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.put("grant_type", List.of(GRANT_TYPE));
        formData.put("client_id", List.of(clientId));
        formData.put("username", List.of(username));
        formData.put("password", List.of(password));
        formData.put("client_secret", List.of(clientSecret));

        String authServerUrl = oAuth2ResourceServerProperties.getJwt().getIssuerUri() +
                        "/protocol/openid-connect/token";

        var request = new HttpEntity<>(formData, httpHeaders);
        return restTemplate.postForObject(authServerUrl, request, TokenInfo.class);
    }

    public record TokenInfo(@JsonProperty("access_token") String accessToken, @JsonProperty("refresh_token") String refreshToken){
    }
}
