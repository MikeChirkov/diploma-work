package ru.ertelecom.attachedfiles;

import ru.ertelecom.attachedfiles.objects.PackageAttached;
import ru.ertelecom.attachedfiles.objects.SqlPlusAttached;
import ru.ertelecom.config.MainConfig;
import ru.ertelecom.config.ServerConfig;
import ru.ertelecom.git.GitWork;
import ru.ertelecom.logger.*;

import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для работы с накатными файлами типа .sql
 */
public class SqlAttachedFile implements AttachedFile{

    private static Logger logger = LoggerManager.getLogger(SqlAttachedFile.class);
    private static JiraLogger jiraLogger = JiraLoggerManager.getJiraLogger();
    //  Список зарезервированных слов для всех накатных файлов типа .sql
    private List<String> words = Arrays.asList("server", "schema", "type", "crit", "only_for", "exclude", "body", "spec", "revision");
    //  Список зарезервированных слов для типа oci накатных файлов типа .sql
    private List<String> wordsOci = Arrays.asList("server", "schema", "type", "only_for", "exclude", "body", "spec", "revision");
    //  Список зарезервированных слов для типа oci для отдельных атрибутов
    private List<String> wordsOciOtherAttrs = Arrays.asList("body", "spec", "revision");

    //  Map атрибутов из накатного файла
    private Map<String, String> attrsMap = new HashMap<>();
    //  Map объктов наката данного файла
    private List<Object> attachedContents = new ArrayList<>();
    private String file;
    private String sourceCode;
    private boolean canDeploy;
    private String issueKey;
    /**
     * Конструктор класса SqlAttachedFile
     *
     * @param file  Путь к накатному файлу DEV-<номер задачи>/<имя файла>
     */
    public SqlAttachedFile(String file) {
        this.file = file;
        sourceCode = getSourceCodeFromDeploy();
        issueKey = file.split("/")[0];
        canDeploy = true;
    }

    /**
     * Возвращает исходный код накатного файла из репозитория Deploy
     *
     * @return  Возвращает исходный код накатного файла
     */
    @Override
    public String getSourceCodeFromDeploy() {
        return new GitWork(MainConfig.CONFIG.getDeployRepoPath()).gitGetSourceCode("master",  file);
    }

    /**
     * Возвращает булеву переменную которая обозначает, накатывать данный файл или нет
     *
     * @return  Возвращает результат проверки накатного файла
     */
    @Override
    public boolean getCanDeploy() {
        return canDeploy;
    }

    /**
     * Получает список накатных объектов, перед этим проверяя и создавая их
     *
     * @return  Возвращает список накатных объектов
     */
    @Override
    public List<Object> getAttachedContents() {
        if(attachedContents.isEmpty()){
            getAttrsAttachedFile();
            checkAttributes();
            createAttachedInstance();
        }
        return attachedContents;
    }

    /**
     * Достает атрибуты для их проверки в накатном файле
     *
     */
    private void getAttrsAttachedFile(){
        try{
            //String str = "   --   attrs   (server=db,radius;schema=excellent;type=oci;revision=20190724140608599;only_for=111)";
            //  Разбиваем файл по переносам строки
            String[] parseFile = sourceCode.replace("\r", "" ).split("\n");
            //  Парсим первую строку с атрибутами
            String[] firstString = parseFile[0].split("[()]");
            //  Парсим атрибуты по ;
            String[] attrs = firstString[1].split(";");
            for(String s : attrs){
                //  Достаем имя атрибута
                String attrsName = s.split("=")[0].replace(" ", "");
                //  Достаем значение атрибуты
                String attrsValue = s.split("=")[1].replace(" ", "");
                //  Проверяем совпадает ли название атрибута с зарезервированными словами
                if (words.contains(attrsName)){
                    if (!attrsMap.containsKey(attrsName)){
                        attrsMap.put(attrsName, attrsValue);
                    }
                    else {
                        logger.log(LoggerEventType.ERROR,
                                issueKey,
                                String.format("%s: Атрибуты дублируются", file));
                        jiraLogger.append(String.format("%s: Атрибуты дублируются\n", file));
                        canDeploy = false;
                    }
                }
                else{
                    logger.log(LoggerEventType.ERROR,
                            issueKey,
                            String.format("%s: Неизвестный атрибут: %s", file, attrsName));
                    jiraLogger.append(String.format("%s: Неизвестный атрибут: %s\n", file, attrsName));
                    canDeploy = false;
                }
                //  Проверка на запятую, если её поставят, то в значении будет зарезервированное слово
                if(checkContainsNameInValue(attrsValue)) {
                    logger.log(LoggerEventType.ERROR,
                            issueKey,
                            String.format("%s: В значении атрибута %s находится зарезервированный атрибут: %s",
                                    file, attrsName, attrsValue));
                    jiraLogger.append(String.format("%s: В значении атрибута %s находится зарезервированный атрибут: %s\n",
                            file, attrsName, attrsValue));
                    canDeploy = false;
                }
            }
        }catch (Exception e){
            logger.log(LoggerEventType.ERROR,
                    issueKey,
                    String.format("%s: Ошибка чтения накатного файла\nТекст ошибки: %s", file, e.getMessage()));
            jiraLogger.append(String.format("%s: Ошибка чтения накатного файла\nТекст ошибки: %s\n", file, e.getMessage()));
            canDeploy = false;
        }
    }

    /**
     * Создает инстансы накатных файлов (Package, SQLPlus, Web)
     *
     */
    private void createAttachedInstance(){
        if(getSchema() != null && getServer() != null){
            //  Разбиваем файл по переносам строки
            String[] attrs = sourceCode.replace("\r", "" ).split("\n");
            //  Количество атрибутов, необходимо для разных ревизий
            int countAttrs = 0;
            //  Количество итераций не найденных путей (если указано oci)
            int countEmptyPath = 0;
            //  Атрибуты для пакета
            Map<String, String> attrsPackage = new HashMap<>();
            //  Пробегаем по накатному файлу
            for(int i = 0; i < attrs.length; i++){
                switch (getType()) {
                    case "oci":
                        countEmptyPath++;
                        attrs[i] = attrs[i].replace(" ", "");
                        // Если строка содержит --attrs
                        if (attrs[i].contains("--attrs")) {
                            // Берем то, что в скобках
                            String[] listAttr = attrs[i].split("[()]");
                            // И разбиваем на массив строк с разделителем ;
                            String[] attr = listAttr[1].split(";");
                            for (String s : attr) {
                                String attrsName = s.split("=")[0].replace(" ", "");
                                String attrsValue = s.split("=")[1].replace(" ", "");
                                // Если встретили атрибуты впервые, смотрим все атрибуты
                                if (wordsOci.contains(attrsName) && countAttrs == 0) {
                                    attrsPackage.put(attrsName, attrsValue);
                                    //  Если встретили атрибуты НЕ впервые, смотрим только ревизию, spec и body
                                } else if (wordsOciOtherAttrs.contains(attrsName) && countAttrs > 0) {
                                    attrsPackage.put(attrsName, attrsValue);
                                } else {
                                    logger.log(LoggerEventType.ERROR,
                                            issueKey,
                                            String.format("%s: Неизвестный атрибут: %s", file, attrsName));
                                    jiraLogger.append(String.format("%s: Неизвестный атрибут: %s\n", file, attrsName));
                                    canDeploy = false;
                                }
                            }
                            countAttrs++;
                            //  Если вдруг пустая строка пропускаем шаг
                        }else if(attrs[i].isEmpty()){
                            continue;
                            //  Если @ в начале строки, то это путь к пакету, создаем инстанс пакет
                        }else if(attrs[i].substring(0, 1).equals("@") && !attrs[i].isEmpty() && !attrs[i].contains("--")) {
                            attachedContents.add(new PackageAttached(attrsPackage, attrs[i]));
                            //  Если не встретили ни атрибутов, ни путей к пакету более 3 раз, останавливаемся, накатный файл некорректный
                        }else if(countEmptyPath > 3){
                            logger.log(LoggerEventType.ERROR,
                                    issueKey,
                                    String.format("Не удалось найти путь до пакетов в файле %s. Возможно, у скрипта sqlplus указан тип OCI", file));
                            jiraLogger.append(String.format("Не удалось найти путь до пакетов в файле %s. " +
                                    "Возможно, у скрипта sqlplus указан тип OCI\n", file));
                            canDeploy = false;
                            return;
                        }else{
                            continue;
                        }
                        break;
                    case "sqlplus": {
                        //  Проверяем есть ли в накатном скрипте create or replace
                        if(checkOci()){
                            SqlPlusAttached sqlplus = new SqlPlusAttached(file, attrsMap);
                            attachedContents.add(sqlplus);
                            return;
                        }else{
                            logger.log(LoggerEventType.ERROR,
                                    issueKey,
                                    String.format("%s: Обнаружен пакет в скрипте", file));
                            jiraLogger.append(String.format("%s: Обнаружен пакет в скрипте\n", file));
                            canDeploy = false;
                            return;
                        }
                    }
                    case "ddl": {
                        //  DDL класс не реализован
                        SqlPlusAttached sqlplus = new SqlPlusAttached(file, attrsMap);
                        attachedContents.add(sqlplus);
                        //  Ddl ddl = new Ddl();
                        //  AttachContents.Add(ddl);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Проверка в накатном файле пакета
     *
     * @return  Возвращает результат проверки
     */
    private boolean checkOci(){
        Pattern bodyPattern = Pattern.compile("create +or +replace +package +(?:body +)" +
                "{0,1}([^ ,]*?\\.){0,1}([^ ]*?)(?: +[^ ]*)*? *?(?:\n|\r\n| )+?(?:as|is)", Pattern.CASE_INSENSITIVE);
        Matcher ociCheck = bodyPattern.matcher(sourceCode);
        return !ociCheck.find();
    }

    /**
     * Проверка атрибутов
     * Конкретно для sql файлов, проверка сервера и схемы
     */
    @Override
    public void checkAttributes(){
        if(getSchema() != null && getServer() != null){
            if(!checkServersAndSchema(getServer(), getSchema())){
                logger.log(LoggerEventType.ERROR,
                        issueKey,
                        String.format("%s: Ошибка сервера/схемы", file));
                canDeploy = false;
            }
        }
    }

    /**
     * Проверка сервера и его схем
     *
     * @param servers       Список серверов (может быть один и несколько)
     * @param schema        Схема из атрибутов
     * @return              Возвращает результат проверки
     */
    private boolean checkServersAndSchema(List<String> servers, String schema){
        boolean flag = false;
        for(String server : servers){
            if(checkServer(server)){
                if(checkSchema(server, schema)){
                    flag = true;
                }else{
                    logger.log(LoggerEventType.ERROR,
                            issueKey,
                            String.format("%s: На сервере: %s нет схемы %s", file, server, schema));
                    jiraLogger.append(String.format("%s: На сервере: %s нет схемы %s\n", file, server, schema));
                    canDeploy = false;
                    return false;
                }
            }else{
                logger.log(LoggerEventType.ERROR,
                        issueKey,
                        String.format("%s: Данный сервер отсутствует: %s ", file, server));
                jiraLogger.append(String.format("%s: Данный сервер отсутствует: %s\n", file, server));
                canDeploy = false;
                return false;
            }
        }
        return flag;
    }

    /**
     * Проверка схемы у сервера
     *
     * @param server        Сервер из атрибутов
     * @param schema        Схема из атрибутов
     * @return              Возвращает результат проверки
     */
    private boolean checkSchema(String server, String schema){
        return ServerConfig.CONFIG.getServerMap().get(server.toUpperCase()).contains(schema.toUpperCase());
    }

    /**
     * Проверка сервера в зарезервированных словах
     *
     * @param server        Сервер из атрибутов
     * @return              Возвращает результат проверки
     */
    private boolean checkServer(String server){
        return ServerConfig.CONFIG.getServerMap().containsKey(server.toUpperCase());
    }

    /**
     * Проверяет входит ли в значение атрибута его имя
     * Бывает что программисты ставят запятую вместо точки с запятой
     * И в значение попадает имя атрибута, что неверно
     *
     * @param attr          Значение атрибута
     * @return              Возвращает результат проверки
     */
    private boolean checkContainsNameInValue(String attr){
        for(String word : words) {
            if (attr.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Достает сервер на который необходимо произвести накат из Map атрибутов
     *
     * @return  Возвращает список серверов
     */
    public List<String> getServer() {
        if (attrsMap.containsKey("server")){
            String[] servers = attrsMap.get("server").split(",");
            return Arrays.asList(servers);
        }
        else{
            return null;
        }
    }

    /**
     * Достает схему на которую необходимо произвести накат из Map атрибутов
     *
     * @return  Возвращает схему
     */
    public String getSchema() {
        return attrsMap.getOrDefault("schema", null);
    }

    /**
     * Достает тип наката из Map атрибутов
     *
     * @return  Возвращает тип
     */
    public String getType() {
        if (attrsMap.containsKey("type")){
            if(attrsMap.get("type").equals("ddl"))
                return "sqlplus";
            else
                return attrsMap.get("type");
        }
        else{
            return "oci";
        }
    }
}