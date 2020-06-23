package ru.ertelecom.config;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import ru.ertelecom.logger.Logger;
import ru.ertelecom.logger.LoggerEventType;
import ru.ertelecom.logger.LoggerManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Содержит базовые конфиги:
 * - логин и пароль от баз
 * - пути к локальным репозиториям
 * ДОБАВЛЯЕМ СЮДА ПО МЕРЕ ДОБАВЛЕНИЯ ИНФОРМАЦИИ В КОНФИГ
 */
public enum MainConfig {
    CONFIG;

    private Logger logger = LoggerManager.getLogger(LoggerConfig.class);
    private @Getter String engineRepoPath;
    private @Getter String deployRepoPath;
    private @Getter String wwwDataRepoPath;

    private @Getter String dbUsername;
    private @Getter String dbPassword;
    private @Getter String sqlPlusPath;


    /**
     * Конструктор перечисления в котором инициализируются переменные.
     */
    MainConfig() {
        try {
            String text = ReadConfig();

            JSONObject repo = new JSONObject(new JSONObject(text).get("gitRepositories").toString());
            engineRepoPath =  getPath(repo.getString("engine"));
            deployRepoPath = getPath(repo.getString("deploy"));
            wwwDataRepoPath = getPath(repo.getString("www-data"));

            JSONObject connect = new JSONObject(new JSONObject(text).get("dbConnectData").toString());
            dbUsername = connect.getString("username");
            dbPassword = connect.getString("password");
            sqlPlusPath = connect.getString("sqlPlusPath");

        } catch (Exception e) {
            logger.log(LoggerEventType.ERROR,
                    String.format("Ошибка загрузки базовой конфигураци\nТекст ошибки: %s", e.getMessage()));
        }
    }

    /**
     * Прочитать базовый конфиг из файла
     *
     * @return Возвращает содержимое файла в формате UTF_8
     */
    private String ReadConfig() throws Exception {
        File file = new File(ConfigPath.main_config_path);
        return FileUtils.readFileToString(file, "utf-8");
    }

    private String getPath(String pathStr){
        Path path = Paths.get(pathStr);
        if (Files.exists(path)) {
            return path.toString();
        }else{
            return null;
        }
    }
}
