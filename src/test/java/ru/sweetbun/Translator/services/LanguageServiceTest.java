package ru.sweetbun.Translator.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.sweetbun.Translator.dao.LanguagesDAO;
import ru.sweetbun.Translator.dto.SupportedLanguagesDTO;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LanguageServiceTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private LanguagesDAO languagesDAO;
    @InjectMocks
    private LanguagesService languagesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void init_shouldRequestSupportedLanguages() {
        LanguagesService spyService = spy(languagesService);
        doNothing().when(spyService).requestSupportedLanguages();

        spyService.init();

        verify(spyService, times(1)).requestSupportedLanguages();
    }

    @Test
    void requestSupportedLanguages_shouldSaveLanguages_whenResponseIsOk() throws JsonProcessingException {
        String jsonResponse = "{\"languages\":[{\"code\":\"en\",\"name\":\"English\"}]}";
        ResponseEntity<String> response = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(new ObjectMapper().readTree(jsonResponse));
        when(objectMapper.treeToValue(any(JsonNode.class), eq(SupportedLanguagesDTO.class)))
                .thenReturn(new SupportedLanguagesDTO("en", "English"));

        languagesService.requestSupportedLanguages();

        verify(languagesDAO, times(1)).saveAll(anyList());
    }

    @Test
    void requestSupportedLanguages_shouldThrowRuntimeException_whenResponseIsNotOk() {
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.BAD_REQUEST);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            languagesService.requestSupportedLanguages();
        });

        assertEquals("Error, status: 400 BAD_REQUEST", exception.getMessage());
    }

    @Test
    void getAllLangs_shouldReturnAllLanguages() {
        List<SupportedLanguagesDTO> languages = Collections
                .singletonList(new SupportedLanguagesDTO("en", "English"));
        when(languagesDAO.findAll()).thenReturn(languages);

        List<SupportedLanguagesDTO> result = languagesService.getAllLangs();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("en", result.getFirst().getCode());
        assertEquals("English", result.getFirst().getName());
    }
}
