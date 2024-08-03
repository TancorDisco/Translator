package ru.sweetbun.Translator.dto;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class LanguagesRequestDTO {

    @Value("${yandex.translate.folder-id}")
    private String folderId;
}
