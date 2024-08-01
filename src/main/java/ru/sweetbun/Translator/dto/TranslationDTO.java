package ru.sweetbun.Translator.dto;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class TranslationDTO {

    @Value("${yandex.translate.folder-id}")
    private String folderId;

    private String texts;

    private String targetLanguageCode;

    private String sourceLanguageCode;
}
