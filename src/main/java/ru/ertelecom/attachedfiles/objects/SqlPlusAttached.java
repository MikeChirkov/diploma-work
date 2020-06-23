package ru.ertelecom.attachedfiles.objects;

import lombok.Getter;
import ru.ertelecom.config.MainConfig;
import ru.ertelecom.git.GitWork;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Класс для работы с накатными файлами с типом sqlplus и ddl
 */
public class SqlPlusAttached {
    // Атрибуты из накатного файла
    private Map<String, String> attrsMap;

    private @Getter String pathToFile;
    private @Getter String sourceCode;
    private @Getter List<String> server;
    private @Getter String schema;
    private @Getter List<String> onlyFor;
    private @Getter List<String> exclude;
    private @Getter boolean crit;

    /**
     * Конструктор класса SqlPlus
     *
     * @param attrsMap  Map с атрибутами из накатного файла
     * @param pathToFile    Путь к файлу в репозитории Deploy
     */
    public SqlPlusAttached(String pathToFile, Map<String, String> attrsMap) {
        this.pathToFile = pathToFile;
        this.attrsMap = attrsMap;
        sourceCode = getSourceCodeFromDeploy();
        this.server = getServerFromMap();
        this.schema = getSchemaFromMap();
        this.onlyFor = getOnlyForFromMap();
        this.exclude = getExcludeFromMap();
        this.crit = getCritFromMap();
    }

    /**
     * Возвращает исходный код файла из репозитория Deploy
     *
     * @return  Возвращает исходный код файла
     */
    private String getSourceCodeFromDeploy() {
        return new GitWork(MainConfig.CONFIG.getDeployRepoPath()).gitGetSourceCode("master",  pathToFile);
    }

    /**
     * Возвращает полный путь к накатному файлу со скриптом
     *
     * @return  Возвращает путь к скрипту
     */
    public String getFullPathToAttached() {
        return MainConfig.CONFIG.getDeployRepoPath().replace(".git", "") +
                pathToFile.replace("/", "\\");
    }
    /**
     * Достает сервера на которые будет накатывться скрипт из Map атрибутов
     *
     * @return  Возвращает список серверов
     */
    private List<String> getServerFromMap() {
        return getListAttribute("server");
    }

    /**
     * Достает схему на которую будет накатываться скрипт из Map атрибутов
     *
     * @return  Возвращает схему
     */
    private String getSchemaFromMap() {
        return getStringAttribute("schema");
    }

    /**
     * Достает список городов на которые будет накатываться скрипт из Map атрибутов
     *
     * @return  Возвращает список городов на которые будет накатываться скрипт
     */
    private List<String> getOnlyForFromMap() {
        return getListAttribute("only_for");
    }

    /**
     * Достает список городов на которые НЕ будет накатываться скрипт из Map атрибутов
     *
     * @return  Возвращает список городов на которые НЕ будет накатываться скрипт
     */
    private List<String> getExcludeFromMap() {
        return getListAttribute("exclude");
    }

    /**
     * Определение, насколько критичен скрипт. Останавливать ли накат при падении скрипта.
     *
     * @return  Возвращает true если скрипт критичный, false если нет
     */
    private boolean getCritFromMap() {
        if(attrsMap.containsKey("crit")){
            return attrsMap.get("crit").equals("true");
        }else{
            return false;
        }
    }

    /**
     * Достает необходимый String атрибут из Map атрибутов
     *
     * @param attrName  Название атрибута
     * @return          Возвращает необходимый String атрибут
     */
    private String getStringAttribute(String attrName){
        return attrsMap.getOrDefault(attrName, null);
    }


    /**
     * Достает необходимый множественный атрибут из Map атрибутов
     *
     * @param attrName  Название атрибута
     * @return          Возвращает необходимый множественный атрибут
     */
    private List<String> getListAttribute(String attrName){
        if (attrsMap.containsKey(attrName)){
            String[] attrs = attrsMap.get(attrName).split(",");
            return Arrays.asList(attrs);
        }
        else{
            return null;
        }
    }

}
