package ru.ertelecom.attachedfiles.objects;

import lombok.Getter;
import ru.ertelecom.config.MainConfig;
import ru.ertelecom.git.GitWork;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для работы с пакетами в накатных файлах с типом oci
 */
public class PackageAttached {

    private static final String PACKAGE_BODY_REGEXP = "create +or +replace +package +body +([^ ,\n]*?\\.){0,1}([^ \n]*?)" +
            "([^ ,\n]*?\\.){0,1}(?: +[^ \n]*)*? *?(?:\n|\r\n| )+?(?:as|is)";
    private static final String PACKAGE_SPEC_REGEXP = "create +or +replace +package +(?!body)([^ ,\n]*?\\.){0,1}([^ \n]*?)" +
            "(?: +[^ \n]*)*? *?(?:\n|\r\n| )+?(?:as|is)";

    // Атрибуты из накатного файла
    private Map<String, String> attrsPackage;

    private @Getter String pathToFile;
    private @Getter String sourceCode;
    private @Getter String revision;
    private @Getter List<String> server;
    private @Getter String schema;
    private @Getter List<String> onlyFor;
    private @Getter List<String> exclude;
    private @Getter boolean deployBody;
    private @Getter boolean deploySpec;

    /**
     * Конструктор класса Package
     *
     * @param attrsPackage  Map с атрибутами из накатного файла
     * @param pathToFile    Путь к файлу в репозитории Engine
     */
    public PackageAttached(Map<String, String> attrsPackage, String pathToFile) {

        this.attrsPackage = attrsPackage;
        this.pathToFile = pathToFile;
        this.revision = getRevisionFromMap();
        this.server = getServerFromMap();
        this.schema = getSchemaFromMap();
        this.onlyFor = getOnlyForFromMap();
        this.exclude = getExcludeFromMap();
        this.deployBody = getDeployBodyFromMap();
        this.deploySpec = getDeploySpecFromMap();
        this.sourceCode = getSourceCodeFromEngine();
    }

    /**
     * Возвращает исходный код файла c ревизией из репозитория Engine
     *
     * @return  Возвращает исходный код файла
     */
    private String getSourceCodeFromEngine() {
        return new GitWork(MainConfig.CONFIG.getEngineRepoPath()).gitGetSourceCode(revision,  getNeedPath());
    }

    /**
     * Возвращает путь к фалу в репозитории Engine
     *
     * @return  Возвращает исходный код файла
     */
    private String getNeedPath(){
        return pathToFile.replace("@R:\\", "").replace("\\", "/");
    }

    /**
     * Возвращает имя пакета
     *
     * @return  Возвращает имя пакета
     */
    public String getNamePackage(){
        //  Получаем путь к файлу
        String[] file = pathToFile.replace("\\", "/").split("/");
        //  Достаем имя пакета
        return file[file.length - 1].split("\\.")[0].toUpperCase();
    }
    /**
     * Достает содержимое spec из пакета
     *
     * @return  Возвращает найденный spec, если не нашел, то пустая строка
     */
    public String getSpecGit() {
        Pattern bodyPattern = Pattern.compile(PACKAGE_SPEC_REGEXP, Pattern.CASE_INSENSITIVE);
        Matcher bodyMatcher = bodyPattern.matcher(sourceCode);
        if (bodyMatcher.find()) return sourceCode.substring(sourceCode.indexOf(bodyMatcher.group()),
                sourceCode.indexOf(getIndexBody()));
        return "";
    }

    /**
     * Достает первое вхождение body
     *
     * @return  Возвращает найденный body, если не нашел, то пустая строка
     */
    private String getIndexBody() {
        Pattern bodyPattern = Pattern.compile(PACKAGE_BODY_REGEXP, Pattern.CASE_INSENSITIVE);
        Matcher bodyMatcher = bodyPattern.matcher(sourceCode);
        if (bodyMatcher.find()) return bodyMatcher.group();
        return "";
    }

    /**
     * Достает содержимое body из пакета
     *
     * @return  Возвращает найденный body, если не нашел, то пустая строка
     */
    public String getBodyGit() {
        Pattern bodyPattern = Pattern.compile(PACKAGE_BODY_REGEXP, Pattern.CASE_INSENSITIVE);
        Matcher bodyMatcher = bodyPattern.matcher(sourceCode);
        if (bodyMatcher.find()) return sourceCode.substring(sourceCode.indexOf(bodyMatcher.group()));
        return "";
    }

    /**
     * Достает сервера на которые будет накатывться пакет из Map атрибутов
     *
     * @return  Возвращает список серверов
     */
    private List<String> getServerFromMap() {
        return getListAttribute("server");
    }

    /**
     * Достает схему на которую будет накатываться пакет из Map атрибутов
     *
     * @return  Возвращает схему
     */
    private String getSchemaFromMap() {
        return getStringAttribute("schema");
    }

    /**
     * Достает ревизию которая будет накатываться из Map атрибутов
     *
     * @return  Возвращает ревизию
     */
    private String getRevisionFromMap() {
        return getStringAttribute("revision");
    }



    /**
     * Достает список городов на которые будет накатываться пакет из Map атрибутов
     *
     * @return  Возвращает список городов на которые будет накатываться пакет
     */
    private List<String> getOnlyForFromMap() {
        return getListAttribute("only_for");
    }

    /**
     * Достает список городов на которые НЕ будет накатываться пакет из Map атрибутов
     *
     * @return  Возвращает список городов на которые НЕ будет накатываться пакет
     */
    private List<String> getExcludeFromMap() {
        return getListAttribute("exclude");
    }

    /**
     * Накатываем ли body
     *
     * @return  Возвращает true если катем, false если НЕ катаем
     */
    private boolean getDeployBodyFromMap() {
        return getBooleanAttribute("body");
    }

    /**
     * Накатываем ли spec
     *
     * @return  Возвращает true если катем, false если НЕ катаем
     */
    private boolean getDeploySpecFromMap() {
        return getBooleanAttribute("spec");
    }

    /**
     * Достает необходимый String атрибут из Map атрибутов
     *
     * @param attrName  Название атрибута
     * @return          Возвращает необходимый String атрибут
     */
    private String getStringAttribute(String attrName){
        return attrsPackage.getOrDefault(attrName, null);
    }

    /**
     * Достает из Map аттрибуты с булевым значением
     *
     * @param attrName  Название атрибута
     * @return          Возвращает true/false необходимого аттрибута
     */
    private boolean getBooleanAttribute(String attrName){
        if(attrsPackage.containsKey(attrName)){
            return attrsPackage.get(attrName).equals("true");
        }else{
            return true;
        }
    }

    /**
     * Достает необходимый множественный атрибут из Map атрибутов
     *
     * @param attrName  Название атрибута
     * @return          Возвращает необходимый множественный атрибут
     */
    private List<String> getListAttribute(String attrName){
        if (attrsPackage.containsKey(attrName)){
            String[] attrs = attrsPackage.get(attrName).split(",");
            return Arrays.asList(attrs);
        }
        else{
            return null;
        }
    }

}
