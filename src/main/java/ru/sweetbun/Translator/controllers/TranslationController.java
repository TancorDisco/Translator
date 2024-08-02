package ru.sweetbun.Translator.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sweetbun.Translator.dto.TranslationDTO;
import ru.sweetbun.Translator.services.TranslationService;

@Controller
@RequestMapping("/translator")
public class TranslationController {

    private final TranslationService translationService;

    @Autowired
    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping()
    public String index(Model model) {
        model.addAttribute("translation", new TranslationDTO());
        return "translator/index";
    }

    @ResponseBody
    @PostMapping("/translate")
    public String translate(@ModelAttribute("translation") TranslationDTO translationDTO, HttpServletRequest request) {
        return translationService.translate(translationDTO, request);
    }
}
