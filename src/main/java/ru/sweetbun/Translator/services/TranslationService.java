package ru.sweetbun.Translator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.sweetbun.Translator.dto.TranslationDTO;

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
        final String url = "https://translate.api.cloud.yandex.net/translate/v2/translate";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + apiKey);

        HttpEntity<TranslationDTO> request = new HttpEntity<>(translationDTO, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println(response.getBody());
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to translate, status: " + response.getStatusCode());
        }
    }
}
