package com.careercrawler.views;

import com.careercrawler.beans.CareerCrawlerData;
import com.careercrawler.beans.JobListing;
import com.careercrawler.services.CareerCrawlerMetricService;
import com.careercrawler.services.DataPersistenceService;
import com.careercrawler.services.JobListingSearchService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;


@PermitAll
@Route(value="")
@PageTitle("Dashboard")
@Log4j2
public class CareerCrawlerDashboardView extends VerticalLayout {
    private CareerCrawlerData careerCrawlerData;
    private JobListingSearchService jobListingSearchService;
    private CareerCrawlerMetricService careerCrawlerMetricService;
    private DataPersistenceService dataPersistenceService;

    public CareerCrawlerDashboardView(@Autowired CareerCrawlerData careerCrawlerData, @Autowired JobListingSearchService jobListingSearchService,
                                      @Autowired CareerCrawlerMetricService careerCrawlerMetricService, @Autowired DataPersistenceService dataPersistenceService) {
        this.careerCrawlerData = careerCrawlerData;
        this.jobListingSearchService = jobListingSearchService;
        this.careerCrawlerMetricService = careerCrawlerMetricService;
        this.dataPersistenceService = dataPersistenceService;

        setSpacing(false);

        H2 header = new H2("CareerCrawler Dashboard");
        header.addClassNames(LumoUtility.Margin.Top.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);
        add(header);

        addSearchForm();
        addMetricAndPersistenceForm();
        
        if (careerCrawlerData != null && careerCrawlerData.getJobListingsList().size() > 0) {
            if(careerCrawlerData.isMetricDataAvailable()) {
                addMetricLayout();
            }

            addJobListingTable();
        }
        
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
        getStyle().set("padding-left", "15%");
        getStyle().set("padding-right", "15%");
    }

    private void addMetricLayout() {
        HorizontalLayout metricLayout = new HorizontalLayout();
        metricLayout.setSizeFull();

        if (careerCrawlerData.getGroupedAndCountedBenefits() != null) {
            Chart chart = new Chart(ChartType.PIE);

            Configuration conf = chart.getConfiguration();

            conf.setTitle("Job benefits");

            PlotOptionsPie plotOptions = new PlotOptionsPie();
            plotOptions.setAllowPointSelect(true);
            plotOptions.setCursor(Cursor.POINTER);
            plotOptions.setShowInLegend(true);
            conf.setPlotOptions(plotOptions);

            DataSeries series = new DataSeries();

            for (String benefit : careerCrawlerData.getGroupedAndCountedBenefits().keySet()) {
                series.add(new DataSeriesItem(benefit, (Number) careerCrawlerData.getGroupedAndCountedBenefits().get(benefit)));
            }

            conf.setSeries(series);
            chart.setVisibilityTogglingDisabled(true);

            metricLayout.add(chart);
        }

        VerticalLayout averageSalaryLayout = new VerticalLayout();
        averageSalaryLayout.add(new Label("Average Minimum Salary: " + careerCrawlerData.getAverageMinimumSalary()));
        averageSalaryLayout.add(new Label("Average Maximum Salary: " + careerCrawlerData.getAverageMaximumSalary()));

        metricLayout.add(averageSalaryLayout);

        add(metricLayout);
    }

    private void addMetricAndPersistenceForm() {
        FormLayout formLayout = new FormLayout();
        if (careerCrawlerData != null && careerCrawlerData.getJobListingsList().size() > 0) {
            Button metricsButton = new Button("Generate Metrics");
            metricsButton.setWidth("300px");
            metricsButton.addClickListener(clickEvent -> {
                log.info("Requesting calculation of job listing metrics.");
                careerCrawlerMetricService.generateCareerCrawlerMetrics();
                UI.getCurrent().getPage().reload();
            });
            formLayout.add(metricsButton, 1);

            Button saveButton = new Button("Save Data");
            saveButton.setWidth("300px");
            saveButton.addClickListener(clickEvent -> {
                log.info("Requesting persistence of career crawler data.");
                dataPersistenceService.saveCareerCrawlerData();
                Notification.show("Data saved!", 1500, Notification.Position.MIDDLE);
            });
            formLayout.add(saveButton, 1);
        }

        Button loadButton = new Button("Load Data");
        loadButton.setWidth("300px");
        loadButton.addClickListener(clickEvent -> {
            log.info("Requesting loading of career crawler data.");
            dataPersistenceService.loadCareerCrawlerData();
            UI.getCurrent().getPage().reload();
        });
        formLayout.add(loadButton, 1);

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 6));
        formLayout.getStyle().set("padding-bottom", "30px");
        add(formLayout);
    }

    private void addJobListingTable() {
        Grid<JobListing> jobListingGrid = new Grid<>(JobListing.class, false);

        jobListingGrid.addColumn(new ComponentRenderer<>(jobListing -> {
            Image companyLogo = new Image(jobListing.getLogoUrl(), "Logo");
            companyLogo.setWidth("64px");
            companyLogo.setHeight("64px");
            return companyLogo;
        })).setHeader("Logo");

        jobListingGrid.addColumn(JobListing::getCompanyName).setHeader("Company name");

        jobListingGrid.addColumn(LitRenderer
                .<JobListing>of("<a href=\"${item.jobListingUrl}\">Job listing</a>")
                .withProperty("jobListingUrl", JobListing::getJobListingUrl)).setHeader("External link");

        jobListingGrid.addColumn(JobListing::getMinimumMonthlySalary).setHeader("Minimum Monthly Salary");
        jobListingGrid.addColumn(JobListing::getMaximumMonthlySalary).setHeader("Maximum Monthly Salary");
        jobListingGrid.addColumn(JobListing::getMinimumYearlySalary).setHeader("Minimum Yearly Salary");
        jobListingGrid.addColumn(JobListing::getMaximumYearlySalary).setHeader("Maximum Yearly Salary");

        jobListingGrid.addColumn(new ComponentRenderer<>(jobListing -> {
            Details details = new Details("Benefits");

            VerticalLayout layout = new VerticalLayout();
            for (String benefit : jobListing.getBenefits()) {
                layout.add(new Label(benefit));
            }
            details.addContent(layout);
            return details;
        })).setHeader("Benefits");

        jobListingGrid.setItems(careerCrawlerData.getJobListingsList());
        add(jobListingGrid);
    }

    private void addSearchForm() {
        TextField jobDescriptionField = new TextField("Job description");
        if (careerCrawlerData.getJobDescriptionSearchTerm() != null) jobDescriptionField.setValue(careerCrawlerData.getJobDescriptionSearchTerm());
        jobDescriptionField.setErrorMessage("This field is required");

        TextField locationField = new TextField("Location");
        if (careerCrawlerData.getJobLocation() != null) locationField.setValue(careerCrawlerData.getJobLocation());
        locationField.setErrorMessage("This field is required");

        IntegerField pagesField = new IntegerField("Pages");
        pagesField.setValue(careerCrawlerData.getPageCount());
        pagesField.setHelperText("Max 50 pages");
        pagesField.setMin(1);
        pagesField.setMax(50);
        pagesField.setValue(1);
        pagesField.setWidth("200px");
        pagesField.setStepButtonsVisible(true);

        Button searchButton = new Button("Search");
        searchButton.setWidth("200px");
        searchButton.addClickListener(clickEvent -> {
            log.info(String.format("Submitting search form with values: jobDescription=%s, location=%s, pages=%s", jobDescriptionField.getValue(), locationField.getValue(), pagesField.getValue()));
            careerCrawlerData.setJobDescriptionSearchTerm(jobDescriptionField.getValue());
            careerCrawlerData.setJobLocation(locationField.getValue());
            careerCrawlerData.setPageCount(pagesField.getValue());
            jobListingSearchService.findJobDetailUrlsByJobDescriptionAndLocation(jobDescriptionField.getValue(), locationField.getValue(), pagesField.getValue());
            UI.getCurrent().getPage().reload();
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(jobDescriptionField, 2);
        formLayout.add(locationField, 2);
        formLayout.add(pagesField, 1);
        formLayout.add(searchButton, 1);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 6));
        formLayout.getStyle().set("padding-bottom", "30px");
        add(formLayout);
    }
}