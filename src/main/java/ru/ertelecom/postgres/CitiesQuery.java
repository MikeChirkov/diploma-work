package ru.ertelecom.postgres;

import javafx.geometry.Pos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Класс для запросов к таблице Cities
 */
public class CitiesQuery  {
    /**
     * Пустой конструктор для создания объекта класса
     */
    public CitiesQuery(){ }

    /**
     * Метод, который возвращает информацию по записям из таблицы cities_test
     *
     * @param serverDomian  Название сервера на котором лежат города
     *                      (возможно изменится поле по которому нужен поиск информации по городу)
     * @return Возвращает список словарей с "поле - значение"
     */
    public List<HashMap<String, String>> executeCitiesTest(String serverDomian, PostgresConnector connector){
        try {
            connector.checkConnection();
            Statement statement = connector.getStatement();
            String sqlQuery = "select city_name, server_domain, schema_name, city_time_zone_value from cities_test where server_domain = 'deploy_db'";
            ResultSet rs = statement.executeQuery(sqlQuery);
            List<HashMap<String, String>> list = new ArrayList<>();
            while (rs.next()) {
                HashMap<String, String> map = new HashMap<>();
                map.put("city_name", rs.getString("city_name"));
                map.put("server_domain", rs.getString("server_domain"));
                map.put("schema_name", rs.getString("schema_name"));
                map.put("city_time_zone_value", rs.getString("city_time_zone_value"));
                list.add(map);
            }
            return list;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
