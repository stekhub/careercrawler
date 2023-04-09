package com.careercrawler;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class CareerCrawlerApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(CareerCrawlerApplication.class, args);
	}
}
