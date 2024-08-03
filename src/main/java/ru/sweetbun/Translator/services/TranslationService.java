package ru.sweetbun.Translator.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.sweetbun.Translator.dao.TranslationRequestDAO;
import ru.sweetbun.Translator.dto.TranslationDTO;
import ru.sweetbun.Translator.dto.TranslationRequestDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TranslationService {

    private static final int MAX_WORDS = 100;
    private static final int REQUEST_LIMIT_PER_SECOND = 20;
    private static final int SLEEP_TIME_MS = 1100;
    @Value("${yandex.translate.api-key}")
    private String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TranslationRequestDAO translationRequestDAO;
    private final Semaphore semaphore = new Semaphore(REQUEST_LIMIT_PER_SECOND);
    private final Object rateLimitLock = new Object();
    private int requestCount = 0;

    @Autowired
    public TranslationService(RestTemplate restTemplate, ObjectMapper objectMapper, TranslationRequestDAO translationRequestDAO) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.translationRequestDAO = translationRequestDAO;
    }

    public String translate(TranslationDTO translationDTO, HttpServletRequest request) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        Pattern pattern = Pattern.compile("[\\p{L}\\p{M}']+|[.,!?;]");
        Matcher matcher = pattern.matcher(translationDTO.getTexts());

        List<String> tokens = new ArrayList<>();
        List<Future<String>> futures = new ArrayList<>();
        int wordCount = 0;

        while (matcher.find()) {
            String token = matcher.group();
            tokens.add(token);
            if (token.matches("[\\p{L}\\p{M}']+")) wordCount++;
        }

        if (wordCount > MAX_WORDS) {
            throw new IllegalArgumentException("The text contains more than " + MAX_WORDS + " words!");
        }

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.matches("[\\p{L}\\p{M}']+")) {
                Callable<String> task = () -> {
                    semaphore.acquire();
                    try {
                        synchronized (rateLimitLock) {
                            if (requestCount >= REQUEST_LIMIT_PER_SECOND) {
                                Thread.sleep(SLEEP_TIME_MS);
                                requestCount = 0;
                            }
                            requestCount++;
                        }
                        return translateWord(token, translationDTO);
                    } finally {
                        semaphore.release();
                    }
                };
                futures.add(executorService.submit(task));
            } else {
                futures.add(CompletableFuture.completedFuture(token));
            }
        }

        StringBuilder translatedText = new StringBuilder();
        int sizeOfFutures = futures.size();
        for (int i = 0; i < sizeOfFutures; i++) {
            try {
                String translatedToken = futures.get(i).get();
                translatedText.append(translatedToken);
                if (isPunctuation(translatedToken)) {
                    translatedText.append(" ");
                }
                else if (i < sizeOfFutures - 1 && !isPunctuation(tokens.get(i + 1))) {
                    translatedText.append(" ");
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Error when translating a word: " + e.getMessage());
            }
        }
        executorService.shutdown();

        String finalTranslatedText = translatedText.toString().trim();
        String ipAddress = request.getRemoteAddr();
        translationRequestDAO.save(new TranslationRequestDTO(ipAddress, translationDTO.getTexts(), finalTranslatedText));

        return finalTranslatedText;
    }

    private boolean isPunctuation(String token) {
        return token.matches("[.,!?;]");
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
            return extractTranslateWord(response.getBody());
        } else {
            throw new RuntimeException("Failed to translate, status: " + response.getStatusCode());
        }
    }

    private String extractTranslateWord(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("translations").get(0).path("text").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing the JSON response: " + e.getMessage());
        }
    }
}
