package ru.sweetbun.Translator.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sweetbun.Translator.dto.TranslationDTO;
import ru.sweetbun.Translator.services.LanguagesService;
import ru.sweetbun.Translator.services.TranslationService;

@Controller
@RequestMapping("/translator")
public class TranslationController {

    private final TranslationService translationService;
    private final LanguagesService languagesService;

    @Autowired
    public TranslationController(TranslationService translationService, LanguagesService languagesService) {
        this.translationService = translationService;
        this.languagesService = languagesService;
    }

    @GetMapping()
    public String index(Model model) {
        model.addAttribute("maxWords", TranslationService.MAX_WORDS);
        model.addAttribute("languages", languagesService.getAllLangs());
        model.addAttribute("translation", translationService.getLangsOfLastTranslation());
        model.addAttribute("translatedText", "");
        return "translator/index";
    }

    @PostMapping()
    public String translate(@ModelAttribute("translation") TranslationDTO translationDTO, HttpServletRequest request,
                            Model model) {
        model.addAttribute("translatedText", translationService.translate(translationDTO, request));
        model.addAttribute("maxWords", TranslationService.MAX_WORDS);
        model.addAttribute("translation", translationDTO);
        model.addAttribute("languages", languagesService.getAllLangs());
        return "translator/index";
    }
}
