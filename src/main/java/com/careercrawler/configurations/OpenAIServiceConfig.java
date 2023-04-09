package com.careercrawler.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

import java.time.Duration;

import static com.theokanning.openai.service.OpenAiService.*;

@Configuration
public class OpenAIServiceConfig {
    @Value("${open.ai.private.key}")
    private String openAIPrivateKey;

    @Value("${careercrawler.io.read.timeout}")
    private Integer timeout;

    @Bean
    public OpenAiService openAiService() {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(openAIPrivateKey, Duration.ofMillis(timeout))
                .newBuilder()
                .build();
        Retrofit retrofit = defaultRetrofit(client, mapper);
        OpenAiApi api = retrofit.create(OpenAiApi.class);
        return new OpenAiService(api);
    }
}

