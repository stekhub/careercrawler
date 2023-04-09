package com.careercrawler.services;

import com.careercrawler.beans.CareerCrawlerData;
import jakarta.inject.Inject;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@Log4j2
public class DataPersistenceService {
    @Inject
    private CareerCrawlerData careerCrawlerData;

    private static String FILE_NAME = "career_crawler_data.txt";

    public void saveCareerCrawlerData() {
        log.info("Persisting career crawler data into file.");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME);
            ObjectOutputStream objectOutputStream  = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(careerCrawlerData);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (Exception e) {
            log.error("Failed to persist career crawler data into file: ", e);
        }
    }

    public void loadCareerCrawlerData() {
        log.info("Loading career crawler data from file.");
        try {
            FileInputStream fileInputStream = new FileInputStream(FILE_NAME);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            CareerCrawlerData careerCrawlerDataInput = (CareerCrawlerData) objectInputStream.readObject();
            objectInputStream.close();

            careerCrawlerData.setJobListingsList(careerCrawlerDataInput.getJobListingsList());
            careerCrawlerData.setAverageMinimumSalary(careerCrawlerDataInput.getAverageMinimumSalary());
            careerCrawlerData.setAverageMaximumSalary(careerCrawlerDataInput.getAverageMaximumSalary());
            careerCrawlerData.setGroupedAndCountedBenefits(careerCrawlerDataInput.getGroupedAndCountedBenefits());
        } catch (Exception e) {
            log.error("Failed to load career crawler data from file: ", e);
        }
    }

}
