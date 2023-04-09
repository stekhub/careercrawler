package com.careercrawler.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class CareerCrawlerData implements Serializable {
    private String jobDescriptionSearchTerm;
    private String jobLocation;
    private Integer pageCount = 1;
    private List<JobListing> jobListingsList = new ArrayList<>();
    private Double averageMinimumSalary;
    private Double averageMaximumSalary;
    private Map<String, Object> groupedAndCountedBenefits;

    public boolean isMetricDataAvailable() {
        return averageMaximumSalary != null || averageMinimumSalary != null || groupedAndCountedBenefits != null;
    }
}
