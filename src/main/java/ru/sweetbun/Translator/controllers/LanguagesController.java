package ru.sweetbun.Translator.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sweetbun.Translator.services.LanguagesService;

@Controller
@RequestMapping("/translator")
public class LanguagesController {

    private final LanguagesService languagesService;

    @Autowired
    public LanguagesController(LanguagesService languagesService) {
        this.languagesService = languagesService;
    }

    @GetMapping("/get-supported-languages")
    public String requestSupportedLanguages() {
        languagesService.requestSupportedLanguages();
        return "redirect:/translator";
    }
}
