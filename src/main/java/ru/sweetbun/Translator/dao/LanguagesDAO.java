package ru.sweetbun.Translator.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.sweetbun.Translator.dto.SupportedLanguagesDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LanguagesDAO {
    @Value("${spring.datasource.url}")
    private String URL;
    @Value("${spring.datasource.username}")
    private String USER;
    @Value("${spring.datasource.password}")
    private String PASSWORD;

    public void saveAll(List<SupportedLanguagesDTO> langs) {
        String clearTableQuery = "DELETE FROM supported_languages";
        String insertQuery = "INSERT INTO supported_languages (code, name) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement clearStatement = connection.prepareStatement(clearTableQuery);
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

            clearStatement.executeUpdate();

            for (SupportedLanguagesDTO lang : langs) {
                insertStatement.setString(1, lang.getCode());
                insertStatement.setString(2, lang.getName());
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Error when saving to the database: " + e.getSQLState() + e.getMessage());
        }
    }

    public List<SupportedLanguagesDTO> findAll() {
        String query = "SELECT code, name FROM supported_languages";
        List<SupportedLanguagesDTO> languages = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String code = resultSet.getString("code");
                String name = resultSet.getString("name");
                languages.add(new SupportedLanguagesDTO(code, name));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error when selecting from the database: " + e.getSQLState() + e.getMessage());
        }

        return languages;
    }
}
