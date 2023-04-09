package com.careercrawler.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
public class JobPortalClient {
    @Inject
    private RestTemplate restTemplate;

    public JsonNode callJobPortalWebservice(String url) {
        log.info("Starting job portal webservice call for url=" + url);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("x-requested-with", "XMLHttpRequest");

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            ResponseEntity<String> entity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<Object>(headers),
                    String.class);

            stopWatch.stop();
            log.info(String.format("Finished job portal webservice call for url=%s and executionTime=%s", url, stopWatch.getTotalTimeMillis()));

            return new ObjectMapper().readTree(entity.getBody());
        } catch (Exception e) {
            log.error("Failed to call job portal webservice endpoint for url=" + url, e);
            e.printStackTrace();
        }

        return null;
    }
}