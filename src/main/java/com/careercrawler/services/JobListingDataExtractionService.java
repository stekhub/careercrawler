package com.careercrawler.services;

import com.careercrawler.beans.CareerCrawlerData;
import com.careercrawler.beans.JobListing;
import com.careercrawler.clients.JobPortalClient;
import com.careercrawler.clients.OpenAiClient;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import java.util.stream.StreamSupport;

@Service
@Log4j2
public class JobListingDataExtractionService {
    @Inject
    private CareerCrawlerData careerCrawlerData;

    @Inject
    private JobPortalClient jobPortalClient;

    @Inject
    private OpenAiClient openAiClient;

    @Value("${open.ai.job.listing.data.extraction.command}")
    private String openAIJobListingDataExtractionCommand;

    @Value("${careercrawler.webservice.endpoint.url}")
    private String jobPortalWebserviceEndpointUrl;

    /**
     * Listens to the default queue. For each incoming job listing URL, the job description text is retrieved. The text
     * is sent to the OpenAI API to extract salary and benefit information.
     * @param jobListingUrl
     */
    @JmsListener(destination = "${spring.jms.template.default-destination}")
    public void listenToJobListingQueue(String jobListingUrl) {
        log.info("Starting data extraction for url=" + jobListingUrl);

        if (isUrlAlreadyProcessed(jobListingUrl)) {
            log.info("Skipping duplicate job listing data extraction for url=" + jobListingUrl);
            return;
        }

        try {
            JobListing jobListingData = retrieveInitialJobListingDataFromUrl(jobListingUrl);
            retrieveAdditionalDataFromJobDescriptionWithAiAssistance(jobListingData);

            careerCrawlerData.getJobListingsList().add(jobListingData);

            log.info(String.format("Finished data extraction for URL: %s. Created new jobListingData=%s", jobListingUrl, jobListingData.toString()));
        } catch (Exception e) {
            log.error("Failed data extraction for URL: " + jobListingUrl, e);
        }
    }

    private boolean isUrlAlreadyProcessed(String jobListingUrl) {
        for (JobListing processedJobListingData : careerCrawlerData.getJobListingsList()) {
            if (jobListingUrl.equals(processedJobListingData.getJobListingUrl())) {
                return true;
            }
        }
        return false;
    }

    private JobListing retrieveInitialJobListingDataFromUrl(String jobListingUrl) throws Exception {
        log.info("Fetching initial set of job listing data from url=" + jobListingUrl);

        JsonNode root = jobPortalClient.callJobPortalWebservice(jobListingUrl);

        JobListing jobListingData = new JobListing(
            jobListingUrl,
            root.findValue("jobHeader").get("company").get("name").asText(),
            root.findValue("jobHeader").get("company").get("logoUrl").asText(),
            Jsoup.parse(root.findValue("jobContent").get("text").asText()).text()
        );

        log.info("Finished extraction of initial jobListingData=" + jobListingData.toString());

        return jobListingData;
    }

    private void retrieveAdditionalDataFromJobDescriptionWithAiAssistance(JobListing jobListingData) throws Exception {
        log.info("Using AI assistance to fetch additional data from url=" + jobListingData.getJobListingUrl());

        JsonNode root = openAiClient.callOpenAiWebservice(openAIJobListingDataExtractionCommand + " " + jobListingData.getJobDescription());

        jobListingData.setMinimumMonthlySalary(root.findValue("minimumMonthlySalary").asDouble());
        jobListingData.setMaximumMonthlySalary(root.findValue("maximumMonthlySalary").asDouble());
        jobListingData.setMinimumYearlySalary(root.findValue("minimumYearlySalary").asDouble());
        jobListingData.setMaximumYearlySalary(root.findValue("maximumYearlySalary").asDouble());

        StreamSupport.stream(root.findValue("benefits").spliterator(), false).forEach(node -> {
            jobListingData.getBenefits().add(node.asText());
        });

        log.info("Finished AI assisted extraction of jobListingData=" + jobListingData.toString());
    }
}