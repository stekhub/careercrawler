package com.careercrawler.services;

import com.careercrawler.clients.JobPortalClient;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class JobListingSearchService {

    @Inject
    private JmsTemplate jmsTemplate;

    @Inject
    private JobPortalClient jobPortalClient;

    @Value("${careercrawler.webservice.endpoint.url}")
    private String jobPortalWebserviceEndpointUrl;

    /**
     * Fetches the job listings found for the search parameters. Creates a new message for each job listing and puts it in the default JMS queue.
     * @param jobDescription Search term for the job listing
     * @param location Location the work place
     * @param pageCount Number of pages to be fetched
     */
    public void findJobDetailUrlsByJobDescriptionAndLocation(String jobDescription, String location, Integer pageCount) {
        log.info(String.format("Retrieving job listings for jobDescription=%s, location=%s, pageCount=%s", jobDescription, location, pageCount));

        for (int page = 1; page <= pageCount; page++) {
            JsonNode root = jobPortalClient.callJobPortalWebservice(jobPortalWebserviceEndpointUrl + "?keywords=" + jobDescription + "&locations=" + location + "&page=" + page);

            root.findValues("jobsItem").stream().forEach(node -> {
                String jobListingDetailsUrl = node.get("link").asText();
                jmsTemplate.convertAndSend(jobListingDetailsUrl);
                log.info("Sending job listing to data extraction queue: url=" + jobListingDetailsUrl);
            });

        }
        log.info(String.format("Finished retrieval of job listings for jobDescription=%s, location=%s, pageCount=%s", jobDescription, location, pageCount));
    }
}
