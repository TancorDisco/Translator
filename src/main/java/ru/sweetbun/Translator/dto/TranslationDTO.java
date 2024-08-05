package ru.sweetbun.Translator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@NoArgsConstructor
@Data
public class TranslationDTO {

    @Value("${yandex.translate.folder-id}")
    private String folderId;

    private String texts;

    private String targetLanguageCode;

    private String sourceLanguageCode;

    public TranslationDTO(String texts, String sourceLanguageCode, String targetLanguageCode) {
        this.texts = texts;
        this.sourceLanguageCode = sourceLanguageCode;
        this.targetLanguageCode = targetLanguageCode;
    }
}
