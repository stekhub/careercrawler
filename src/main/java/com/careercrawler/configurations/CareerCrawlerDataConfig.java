package com.careercrawler.configurations;

import com.careercrawler.beans.CareerCrawlerData;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
public class CareerCrawlerDataConfig {
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public CareerCrawlerData careerCrawlerData() {
        return new CareerCrawlerData();
    }
}
