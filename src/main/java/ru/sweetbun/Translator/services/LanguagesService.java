package ru.sweetbun.Translator.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.sweetbun.Translator.dao.LanguagesDAO;
import ru.sweetbun.Translator.dto.LanguagesRequestDTO;
import ru.sweetbun.Translator.dto.SupportedLanguagesDTO;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LanguagesService {

    @Value("${yandex.translate.api-key}")
    private String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LanguagesDAO languagesDAO;

    @Autowired
    public LanguagesService(RestTemplate restTemplate, ObjectMapper objectMapper, LanguagesDAO languagesDAO) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.languagesDAO = languagesDAO;
    }

    @PostConstruct
    public void init() {
        log.info("Request for supported languages...");
        requestSupportedLanguages();
        log.info("The request is successful!");
    }

    public void requestSupportedLanguages() {
        final String url = "https://translate.api.cloud.yandex.net/translate/v2/languages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + apiKey);

        HttpEntity<LanguagesRequestDTO> request = new HttpEntity<>(new LanguagesRequestDTO(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            List<SupportedLanguagesDTO> langs = extractLanguagesFromResponse(response.getBody());
            languagesDAO.saveAll(langs);
        } else {
            throw new RuntimeException("Error, status: " + response.getStatusCode());
        }
    }

    private List<SupportedLanguagesDTO> extractLanguagesFromResponse(String responseBody) {
        List<SupportedLanguagesDTO> languages = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode languagesNode = rootNode.path("languages");

            for (JsonNode langNode : languagesNode) {
                SupportedLanguagesDTO lang = objectMapper.treeToValue(langNode, SupportedLanguagesDTO.class);
                if (lang.getName() != null) languages.add(lang);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return languages;
    }

    public List<SupportedLanguagesDTO> getAllLangs() {
        return languagesDAO.findAll();
    }
}
