package ru.sweetbun.Translator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TranslationRequestDTO {

    private String ipAddress;

    private String inputText;

    private String finalTranslatedText;
}
