package ru.sweetbun.Translator.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.sweetbun.Translator.dto.TranslationRequestDTO;

import java.sql.*;

@Repository
public class TranslationRequestDAO {
    @Value("${spring.datasource.url}")
    private String URL;
    @Value("${spring.datasource.username}")
    private String USER;
    @Value("${spring.datasource.password}")
    private String PASSWORD;

    public void save(TranslationRequestDTO translationRequestDTO) {
        String query = "INSERT INTO translation_requests (ip_address, input_text, translated_text) values (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, translationRequestDTO.getIpAddress());
            preparedStatement.setString(2, translationRequestDTO.getInputText());
            preparedStatement.setString(3, translationRequestDTO.getFinalTranslatedText());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error when saving to the database: " + e.getSQLState());
        }

    }
}
