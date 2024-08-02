package ru.sweetbun.Translator.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.sweetbun.Translator.dto.TranslationDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class TranslationService {

    @Value("${yandex.translate.api-key}")
    private String apiKey;
    private final RestTemplate restTemplate;

    @Autowired
    public TranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String translate(TranslationDTO translationDTO) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        String[] words = translationDTO.getTexts().split("\\s+");
        List<Future<String>> futures = new ArrayList<>();

        for (String word : words) {
            Callable<String> task = () -> translateWord(word, translationDTO);
            futures.add(executorService.submit(task));
        }

        StringBuilder translatedText = new StringBuilder();
        for (Future<String> future : futures) {
            try {
                translatedText.append(future.get()).append(" ");
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Error when translating a word: " + e.getMessage());
            }
        }
        executorService.shutdown();

        return translatedText.toString().trim();
    }

    private String translateWord(String word, TranslationDTO translationDTO) {
        final String url = "https://translate.api.cloud.yandex.net/translate/v2/translate";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + apiKey);

        TranslationDTO wordDTO = new TranslationDTO();
        wordDTO.setFolderId(translationDTO.getFolderId());
        wordDTO.setSourceLanguageCode(translationDTO.getSourceLanguageCode());
        wordDTO.setTargetLanguageCode(translationDTO.getTargetLanguageCode());
        wordDTO.setTexts(word);

        HttpEntity<TranslationDTO> request = new HttpEntity<>(wordDTO, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println(response.getBody());
            return extractTranslateWord(response.getBody());
        } else {
            throw new RuntimeException("Failed to translate, status: " + response.getStatusCode());
        }
    }

    private String extractTranslateWord(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("translations").get(0).path("text").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing the JSON response: " + e.getMessage());
        }
    }
}
