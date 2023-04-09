package com.careercrawler.services;

import com.careercrawler.beans.CareerCrawlerData;
import com.careercrawler.clients.OpenAiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class CareerCrawlerMetricService {
    @Inject
    private CareerCrawlerData careerCrawlerData;

    @Inject
    private OpenAiClient openAiClient;

    @Value("${open.ai.job.listing.benefit.analysis.command}")
    private String openAIJobListingBenefitAnalysisCommand;

    /**
     * Calculates average minimum and maximum monthly salary and the top 10 benefits offered by companies.
     */
    public void generateCareerCrawlerMetrics() {
        log.info("Starting metric calculation.");

        careerCrawlerData.setAverageMinimumSalary(careerCrawlerData.getJobListingsList().stream().mapToDouble(jobListing -> {
            if (jobListing.getMinimumYearlySalary() != null && jobListing.getMinimumYearlySalary() > 0) return jobListing.getMinimumYearlySalary() / 14;
            if (jobListing.getMinimumMonthlySalary() != null && jobListing.getMinimumMonthlySalary() > 0) return jobListing.getMinimumMonthlySalary();
            return 0d;
        }).filter(i -> i > 0).average().orElse(0d));

        careerCrawlerData.setAverageMaximumSalary(careerCrawlerData.getJobListingsList().stream().mapToDouble(jobListing -> {
            if (jobListing.getMaximumYearlySalary() != null && jobListing.getMaximumYearlySalary() > 0) return jobListing.getMaximumYearlySalary() / 14;
            if (jobListing.getMaximumMonthlySalary() != null && jobListing.getMaximumMonthlySalary() > 0) return jobListing.getMaximumMonthlySalary();
            return 0d;
        }).filter(i -> i > 0).average().orElse(0d));

        List<String> accumulatedBenefits = careerCrawlerData.getJobListingsList().stream()
                .flatMap(jobListing -> jobListing.getBenefits().stream()).collect(Collectors.toList());

        try {
            log.info("Using AI assistance to group and count accumulated benefit list.");
            JsonNode root = openAiClient.callOpenAiWebservice(openAIJobListingBenefitAnalysisCommand + " " + accumulatedBenefits.toString());
            if (careerCrawlerData != null) {
                careerCrawlerData.setGroupedAndCountedBenefits(new ObjectMapper().readValue(root.toString(), HashMap.class));
            }
        } catch (Exception e) {
            log.error("Failed to use AI assistance to group and count accumulated benefit list.", e);
        }
    }
}