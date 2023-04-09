package com.careercrawler.beans;

import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class JobListing implements Serializable {
    @NonNull
    private String jobListingUrl;

    @NonNull
    private String companyName;

    @NonNull
    private String logoUrl;

    @NonNull
    private String jobDescription;

    private Double minimumMonthlySalary;

    private Double maximumMonthlySalary;

    private Double minimumYearlySalary;

    private Double maximumYearlySalary;

    private Set<String> benefits = new HashSet<>();

    public String getBenefitsAsString() {
        return String.join(", ", benefits);
    }
}
