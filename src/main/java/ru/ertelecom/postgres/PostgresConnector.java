package ru.ertelecom.postgres;

import ru.ertelecom.config.LoggerConfig;

import java.sql.*;

/**
 * Класс для подключения к PostgreSQL
 */
public class PostgresConnector {

    private Connection connection;
    private Statement statement;

    public PostgresConnector(){
    }

    /**
     * Метод для соединения с СУБД PostgreSQL
     */
    public void connect() {
        if(checkDriver()){
            connection = createConnection();
            if (connection != null) {
                System.out.println("Успешное подключение к PostgreSQL.");
            } else {
                System.out.println("Не удалось установить соединение с PostgreSQL.");
            }
        }
    }

    /**
     * Метод для проверки JDBC драйвера PostgreSQL
     */
    private boolean checkDriver(){
        try {
            Class.forName("org.postgresql.Driver");
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver не найден. Подключите данную библиотеку.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Создание соединения с PostgreSQL
     */
    private Connection createConnection(){
        try {
            return DriverManager
                    .getConnection(LoggerConfig.CONFIG.getLogUrl(),
                            LoggerConfig.CONFIG.getLogUsername(),
                            LoggerConfig.CONFIG.getLogPassword());
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к PostgreSQL.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Проверка соединения с PostgreSQL
     */
    public void checkConnection() throws SQLException {
        if (connection == null || !connection.isValid(0)) {
            System.out.println("Соединение с PostgreSQL потеряно, восстанавливаю...");
            connect();
        }
    }

    /**
     * Getter для Statement
     */
    public Statement getStatement() throws SQLException {
        return connection.createStatement();
    }
}

