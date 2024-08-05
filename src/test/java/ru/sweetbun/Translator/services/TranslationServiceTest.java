package ru.sweetbun.Translator.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.sweetbun.Translator.dao.TranslationsDAO;
import ru.sweetbun.Translator.dto.TranslationDTO;
import ru.sweetbun.Translator.dto.TranslationRequestDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TranslationServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private TranslationsDAO translationsDAO;
    @Mock
    private HttpServletRequest request;
    @InjectMocks
    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void translate_shouldReturnTranslatedTextAndSaveRequest() throws JsonProcessingException {
        TranslationDTO translationDTO = new TranslationDTO("World", "en", "ru");
        String jsonResponse = "{\"translations\":[{\"text\":\"Мир\"}]}";
        ResponseEntity<String> response = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        when(request.getRemoteAddr()).thenReturn("0.0.0.1");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(new ObjectMapper().readTree(jsonResponse));

        String result = translationService.translate(translationDTO, request);

        assertNotNull(result);
        assertEquals("Мир", result);
        verify(translationsDAO, times(1)).save(any(TranslationRequestDTO.class));
    }

    @Test
    void translate_shouldThrowException_whenWordCountExceedsLimit() {
        String inputText = "word ".repeat(TranslationService.MAX_WORDS+1);

        TranslationDTO translationDTO = new TranslationDTO();
        translationDTO.setTexts(inputText);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->{
           translationService.translate(translationDTO, request);
        });

        assertEquals("Текст содержит более " + TranslationService.MAX_WORDS + " слов!", ex.getMessage());
    }

    @Test
    void getLangsOfLastTranslation_shouldReturnTranslationDTO() {
        TranslationDTO translationDTO = new TranslationDTO();
        translationDTO.setSourceLanguageCode("en");
        translationDTO.setTargetLanguageCode("ru");

        when(translationsDAO.getLangsOfLastTranslation()).thenReturn(translationDTO);

        TranslationDTO result = translationService.getLangsOfLastTranslation();

        assertNotNull(result);
        assertEquals("en", result.getSourceLanguageCode());
        assertEquals("ru", result.getTargetLanguageCode());
    }
}
