package ru.ertelecom.postgres;

import javafx.geometry.Pos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Класс для запросов к таблице Events и связным таблицам с ней
 */
public class EventsQuery  {

    public EventsQuery(){ }

    /**
     * Метод, который выполняет запрос к PostgreSQL, необходим для возвращения первого попавшегося значения
     *
     * @param sqlQuery      Запрос который необходимо выполнить
     * @param columnLabel   Название столбца по которому выводить значение
     * @param connector     Подключение к PostgreSQL
     *
     * @return Возвращает первый попавшийся значение по нужному столбцу
     */
    public Integer execute(String sqlQuery, String columnLabel, PostgresConnector connector){
        try {
            connector.checkConnection();
            Statement statement = connector.getStatement();
            ResultSet rs = statement.executeQuery(sqlQuery);
            while (rs.next()) {
                return rs.getInt(columnLabel);
            }
            return null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Метод, который выполняет Insert запрос
     *
     * @param sqlQuery      Запрос который необходимо выполнить
     * @param connector     Подключение к PostgreSQL
     */
    public void insertEvents(String sqlQuery, PostgresConnector connector) {
        try {
            Statement statement = connector.getStatement();
            statement.executeUpdate(sqlQuery);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
