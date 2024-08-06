package ru.sweetbun.Translator.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.sweetbun.Translator.services.LanguagesService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class LanguagesControllerTest {

    @Mock
    private LanguagesService languagesService;

    @InjectMocks
    private LanguagesController languagesController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void requestSupportedLanguages_shouldCallServiceAndRedirect() {
        String viewName = languagesController.requestSupportedLanguages();

        verify(languagesService).requestSupportedLanguages();
        assertEquals("redirect:/translator", viewName);
    }
}
