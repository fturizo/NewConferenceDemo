package com.fturizo.demos.session.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@SuppressWarnings("unused")
public class SpeakerService {

    @Bean
    @Profile("integration")
    public HttpServiceProxyFactory httpServiceProxyFactory(@Value("${conference.demo.services.speaker.url}") String baseUrl) {
        var client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build();
    }

    @Bean
    @Profile("integration")
    public SpeakerServiceClient realSpeakerServiceClient(HttpServiceProxyFactory factory){
        return factory.createClient(SpeakerServiceClient.class);
    }

    @Bean
    @Profile("!integration")
    public SpeakerServiceClient dummySpeakerServiceClient(){
        return names -> ResponseEntity.ok().build();
    }
}
