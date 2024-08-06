package ru.sweetbun.Translator.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import ru.sweetbun.Translator.controllers.TranslationController;
import ru.sweetbun.Translator.dto.TranslationDTO;
import ru.sweetbun.Translator.services.LanguagesService;
import ru.sweetbun.Translator.services.TranslationService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TranslationControllerTest {

    @Mock
    private TranslationService translationService;
    @Mock
    private LanguagesService languagesService;
    @Mock
    private HttpServletRequest request;
    @Mock
    Model model;
    @InjectMocks
    private TranslationController translationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void index_shouldReturnIndexView() {
        when(languagesService.getAllLangs()).thenReturn(new ArrayList<>());
        when(translationService.getLangsOfLastTranslation()).thenReturn(new TranslationDTO());

        String viewName = translationController.index(model);

        assertEquals("translator/index", viewName);
        verify(model, times(1)).addAttribute(eq("maxWords"), eq(TranslationService.MAX_WORDS));
        verify(model, times(1)).addAttribute(eq("languages"), anyList());
        verify(model, times(1)).addAttribute(eq("translation"), any(TranslationDTO.class));
        verify(model, times(1)).addAttribute(eq("translatedText"), eq(""));
    }

    @Test
    void translate_shouldReturnIndexViewWithTranslatedText() {
        TranslationDTO translationDTO = new TranslationDTO();
        String translatedText = "Hello";

        when(translationService.translate(any(TranslationDTO.class), any(HttpServletRequest.class)))
                .thenReturn(translatedText);
        when(languagesService.getAllLangs()).thenReturn(new ArrayList<>());

        String viewName = translationController.translate(translationDTO, request, model);

        assertEquals("translator/index", viewName);
        verify(model, times(1)).addAttribute(eq("translatedText"), eq(translatedText));
        verify(model, times(1)).addAttribute(eq("maxWords"), eq(TranslationService.MAX_WORDS));
        verify(model, times(1)).addAttribute(eq("translation"), eq(translationDTO));
        verify(model, times(1)).addAttribute(eq("languages"), anyList());
    }
}
