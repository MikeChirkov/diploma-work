package ru.ertelecom.config;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;

public enum LoggerConfig {
    CONFIG;

    private @Getter String logUsername;
    private @Getter String logPassword;
    private @Getter String logUrl;

    /**
     * Конструктор перечисления в котором инициализируются переменные.
     */
    LoggerConfig() {
        try {
            String text = ReadConfig();

            JSONObject connectLog = new JSONObject(new JSONObject(text).get("logConnectData").toString());
            logUsername = connectLog.getString("username");
            logPassword = connectLog.getString("password");
            logUrl = connectLog.getString("urlPostgres");

        } catch (Exception e) {
            System.out.println(String.format("Ошибка загрузки базовой конфигурации\nТекст ошибки: %s", e.getMessage()));
        }
    }

    /**
     * Прочитать базовый конфиг из файла
     *
     * @return Возвращает содержимое файла в формате UTF_8
     */
    private String ReadConfig() throws Exception {
        File file = new File(ConfigPath.logger_config_path);
        return FileUtils.readFileToString(file, "utf-8");
    }
}
