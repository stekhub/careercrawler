package com.careercrawler.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import jakarta.inject.Inject;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
public class OpenAiClient {
    @Inject
    private OpenAiService openAiService;

    @Value("${open.ai.model}")
    private String openAIModel;

    public JsonNode callOpenAiWebservice(String command) {
        log.info("Calling Open AI webservice for the following command=" + command);

        ChatMessage chatMessage = new ChatMessage("user", command);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .messages(Arrays.asList(chatMessage))
                .temperature(0d)
                .model(openAIModel)
                .build();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            List<ChatCompletionChoice> chatCompletionChoices = openAiService.createChatCompletion(chatCompletionRequest).getChoices();
            if (chatCompletionChoices.size() <= 0) {
                log.error("Open AI did not respond with a completion choice.");
                return null;
            }

            String jsonResponseFromAi = chatCompletionChoices.get(0).getMessage().getContent();

            stopWatch.stop();
            log.info(String.format("Retrieved completion choice from Open AI for the requested command=%s and executionTime=%s", command, stopWatch.getTotalTimeMillis()));

            return new ObjectMapper().readTree(jsonResponseFromAi);
        } catch (Exception e) {
            log.error("Failed to call Open AI webservice endpoint.", e);
            e.printStackTrace();
        }

        return null;
    }
}
