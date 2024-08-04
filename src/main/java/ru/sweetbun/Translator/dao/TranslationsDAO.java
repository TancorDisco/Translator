package ru.sweetbun.Translator.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.sweetbun.Translator.dto.TranslationDTO;
import ru.sweetbun.Translator.dto.TranslationRequestDTO;

import java.sql.*;

@Repository
public class TranslationsDAO {
    @Value("${spring.datasource.url}")
    private String URL;
    @Value("${spring.datasource.username}")
    private String USER;
    @Value("${spring.datasource.password}")
    private String PASSWORD;

    public void save(TranslationRequestDTO translationRequestDTO) {
        String query = "INSERT INTO translation_requests (ip_address, source_language_code, input_text, " +
                "target_language_code, translated_text) values (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, translationRequestDTO.getIpAddress());
            preparedStatement.setString(2, translationRequestDTO.getSourceLanguageCode());
            preparedStatement.setString(3, translationRequestDTO.getInputText());
            preparedStatement.setString(4, translationRequestDTO.getTargetLanguageCode());
            preparedStatement.setString(5, translationRequestDTO.getFinalTranslatedText());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error when saving to the database: " + e.getSQLState());
        }
    }

    public TranslationDTO getLangsOfLastTranslation() {
        String query = "SELECT source_language_code, target_language_code FROM translation_requests ORDER BY id DESC LIMIT 1";
        TranslationDTO translationDTO = new TranslationDTO();

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                translationDTO.setSourceLanguageCode(resultSet.getString("source_language_code"));
                translationDTO.setTargetLanguageCode(resultSet.getString("target_language_code"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error when retrieving last translation from the database: " + e.getSQLState(), e);
        }

        return translationDTO;
    }
}
