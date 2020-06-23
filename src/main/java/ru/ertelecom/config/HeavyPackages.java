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
import java.util.List;


/**
 * Содержит тяжелые пакеты
 */
public enum HeavyPackages {
    CONFIG;

    private Logger logger = LoggerManager.getLogger(HeavyPackages.class);
    private @Getter List<String> heavyPackages = new ArrayList<String>();

    /**
     * Конструктор перечисления в котором инициализируется список тяжелых пакетов.
     */
    HeavyPackages(){
        try {
            String text = ReadConfig();

            JSONArray array = new JSONArray(new JSONObject(text).get("heavyPackages").toString());
            for(Object obj : array){
                heavyPackages.add(obj.toString());
            }
        } catch (Exception e) {
            logger.log(LoggerEventType.ERROR,
                    String.format("Ошибка загрузки тяжелых пакетов из конфигурационного файла\nТекст ошибки: %s", e.getMessage()));
        }
    }

    /**
     * Прочитать тяжелые пакеты из файла
     *
     * @return Возвращает содержимое файла в формате UTF_8
     */
    private static String ReadConfig() throws Exception {
        File file = new File(ConfigPath.heavy_packages_path);
        return FileUtils.readFileToString(file, String.valueOf(StandardCharsets.UTF_8));
    }
}
