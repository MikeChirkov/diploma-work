package ru.ertelecom.config;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import ru.ertelecom.logger.Logger;
import ru.ertelecom.logger.LoggerEventType;
import ru.ertelecom.logger.LoggerManager;
import ru.ertelecom.security.PasswordEncoder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Содержит конфиги для работы с Jira:
 * - логин и пароль, url для подключения к Jira
 * - id шагов заявки, для логики перевода заявки на следующий шаг
 * - компоненты заявки
 * - id необходимых полей в Jira
 * ДОБАВЛЯЕМ СЮДА ПО МЕРЕ ДОБАВЛЕНИЯ ИНФОРМАЦИИ В КОНФИГ
 */
public enum  JiraConfig {
    CONFIG;

    private Logger logger = LoggerManager.getLogger(JiraConfig.class);
    private @Getter String username;
    private @Getter String password;
    private @Getter String url;

    private @Getter Integer sendOneCity;
    private @Getter Integer sendTwoCity;
    private @Getter Integer sendAllCity;
    private @Getter Integer closeIssue;

    private @Getter Integer errorTest;
    private @Getter Integer errorOneCity;
    private @Getter Integer errorTwoCity;
    private @Getter Integer errorAllCity;

    private @Getter Map<String, Integer> issueSteps = new HashMap<String, Integer>();
    private @Getter Map<String, Integer> issueErrorSteps = new HashMap<String, Integer>();

    private @Getter List<String> componentsFields = new ArrayList<String>();
    private @Getter String fixBugs;
    private @Getter String newFunctional;
    private @Getter String optimization;
    private @Getter String groupOperation;

    private @Getter Integer filterDayPlan;
    private @Getter Integer filterNightPlan;
    private @Getter Integer filterDayNoPlan;

    private @Getter String doAfter;
    private @Getter String doBefore;

    private @Getter String businesTechnologistId;
    private @Getter String changeCustomerId;
    private @Getter String codeReviewerId;
    private @Getter String serverId;
    private @Getter String typeId;
    private @Getter String heavyId;
    private @Getter String coordinatorsId;
    private @Getter String firstCityId;
    private @Getter String secondCityId;
    private @Getter String deployedCityId;
    private @Getter String guiltyGroupId;
    private @Getter String risksId;
    private @Getter String subjectAreaId;
    private @Getter String affectedObjectId;
    private @Getter String targetGroupId;
    private @Getter String deviationFromRegulationId;
    private @Getter String dateDeviationFromRegulationId;
    private @Getter String pullRequestInfoId;
    private @Getter String linksIsMarked;

    /**
     * Конструктор перечисления в котором инициализируются переменные.
     */
    JiraConfig() {
        try {
            String text = ReadConfig();

            JSONObject filters = new JSONObject(new JSONObject(text).get("jiraFilters").toString());
            filterDayPlan = filters.getInt("dayPlan");
            filterNightPlan = filters.getInt("nightPlan");
            filterDayNoPlan = filters.getInt("dayNoPlan");

            JSONObject connect = new JSONObject(new JSONObject(text).get("connectData").toString());
            username = connect.getString("username");
            password = PasswordEncoder.decryptPassword(connect.getString("password"),
                    PasswordEncoder.getENCRYPTION_KEY());
            url = connect.getString("url");

            JSONObject steps = new JSONObject(new JSONObject(text).get("issueSteps").toString());
            sendOneCity = steps.getInt("Уведомление одного города");
            sendTwoCity = steps.getInt("Уведомление двух городов");
            sendAllCity = steps.getInt("Уведомление всех городов");
            closeIssue = steps.getInt("Освобождение пакетов");

            JSONObject errorSteps = new JSONObject(new JSONObject(text).get("issueErrorSteps").toString());
            errorTest = errorSteps.getInt("Ошибка наката на тест");
            errorOneCity = errorSteps.getInt("Ошибки. 1 город");
            errorTwoCity = errorSteps.getInt("Ошибки. 2 города");
            errorAllCity = errorSteps.getInt("Ошибки. Все города");

            JSONObject nextSteps = new JSONObject(new JSONObject(text).get("issueNextSteps").toString());
            issueSteps.put("Ожидание наката", nextSteps.getInt("Ожидание наката"));
            issueSteps.put("Накат на один город", nextSteps.getInt("Накат на один город"));
            issueSteps.put("Накат на два города", nextSteps.getInt("Накат на два города"));
            issueSteps.put("Накат на все города", nextSteps.getInt("Накат на все города"));

            JSONObject nextErrorSteps = new JSONObject(new JSONObject(text).get("issueNextErrorSteps").toString());
            issueErrorSteps.put("Ожидание наката", nextErrorSteps.getInt("Ожидание наката"));
            issueErrorSteps.put("Накат на один город", nextErrorSteps.getInt("Накат на один город"));
            issueErrorSteps.put("Накат на два города", nextErrorSteps.getInt("Накат на два города"));
            issueErrorSteps.put("Накат на все города", nextErrorSteps.getInt("Накат на все города"));

            JSONObject components = new JSONObject(new JSONObject(text).get("componentsFields").toString());
            newFunctional = components.getString("newFunctional");
            fixBugs = components.getString("fixBugs");
            optimization = components.getString("optimization");
            groupOperation = components.getString("groupOperation");
            componentsFields.add(newFunctional);
            componentsFields.add(fixBugs);
            componentsFields.add(optimization);
            componentsFields.add(groupOperation);

            JSONObject fields = new JSONObject(new JSONObject(text).get("jiraFields").toString());
            doAfter =  fields.getString("doAfter");
            doBefore = fields.getString("doBefore");
            businesTechnologistId = fields.getString("businesTechnologist");
            changeCustomerId = fields.getString("changeCustomer");
            codeReviewerId = fields.getString("codeReviewer");
            serverId = fields.getString("server");
            typeId = fields.getString("type");
            heavyId = fields.getString("heavy");
            coordinatorsId = fields.getString("coordinators");
            firstCityId = fields.getString("firstCity");
            secondCityId = fields.getString("secondCity");
            deployedCityId = fields.getString("deployedCity");
            guiltyGroupId = fields.getString("guiltyGroup");
            risksId = fields.getString("risks");
            subjectAreaId = fields.getString("subjectArea");
            affectedObjectId = fields.getString("affectedObject");
            targetGroupId = fields.getString("targetGroup");
            deviationFromRegulationId = fields.getString("deviationFromRegulation");
            dateDeviationFromRegulationId = fields.getString("dateDeviationFromRegulation");
            pullRequestInfoId = fields.getString("pullRequestInfo");
            linksIsMarked = fields.getString("linksIsMarked");

        } catch (Exception e) {
            logger.log(LoggerEventType.ERROR,
                    String.format("Ошибка загрузки конфигурации Jira\nТекст ошибки: %s", e.getMessage()));
        }
    }

    /**
     * Прочитать конфиг Jira из файла
     *
     * @return Возвращает содержимое файла в формате UTF_8
     */
    private String ReadConfig() throws Exception {
        File file = new File(ConfigPath.jira_config_path);
        return FileUtils.readFileToString(file, String.valueOf(StandardCharsets.UTF_8));
    }

}