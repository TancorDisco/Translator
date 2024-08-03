package ru.sweetbun.Translator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportedLanguagesDTO {

    private String code;

    private String name;
}
