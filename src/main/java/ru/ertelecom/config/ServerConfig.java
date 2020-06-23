package ru.ertelecom.config;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.ertelecom.logger.Logger;
import ru.ertelecom.logger.LoggerEventType;
import ru.ertelecom.logger.LoggerManager;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Содержит конфиги для серверов и их схем
 */
public enum ServerConfig {
    CONFIG;

    private Logger logger = LoggerManager.getLogger(ServerConfig.class);
    private @Getter
    Map<String, List<String>> serverMap = new HashMap<>();

    /**
     * Конструктор перечисления в котором инициализируются переменные.
     */
    ServerConfig() {
        try {
            String text = ReadConfig();
            JSONArray array = new JSONArray(text);
            for(Object obj : array){
                JSONObject jsonObject = new JSONObject(obj.toString());
                JSONArray jsonArray = new JSONArray(jsonObject.get("schemas").toString());
                serverMap.put(jsonObject.getString("name").toUpperCase(), getSchemas(jsonArray));
            }
        } catch (Exception e) {
            logger.log(LoggerEventType.ERROR,
                    String.format("Ошибка загрузки конфигурации серверов\nТекст ошибки: %s", e.getMessage()));
        }
    }

    /**
     * Получить схемы из JSONArray
     *
     * @return Возвращает список схем
     */
    public List<String> getSchemas(JSONArray jsonArray){
        List<String> list = new ArrayList<>();
        for(Object obj : jsonArray){
            list.add(obj.toString().toUpperCase());
        }
        return list;
    }

    /**
     * Прочитать базовый конфиг из файла
     *
     * @return Возвращает содержимое файла в формате UTF_8
     */
    private String ReadConfig() throws Exception {
        File file = new File(ConfigPath.server_config_path);
        return FileUtils.readFileToString(file, String.valueOf(StandardCharsets.UTF_8));
    }
}