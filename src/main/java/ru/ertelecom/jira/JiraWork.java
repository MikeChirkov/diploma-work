package ru.ertelecom.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import io.atlassian.util.concurrent.Promise;
import lombok.Getter;
import org.json.JSONObject;
import ru.ertelecom.config.JiraConfig;
import ru.ertelecom.config.HeavyPackages;
import ru.ertelecom.jira.factory.CustomAsynchronousJiraRestClientFactory;
import ru.ertelecom.logger.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для работы с Jira
 */
public class JiraWork {

    private Logger logger = LoggerManager.getLogger(JiraWork.class);
    private JiraLogger jiraLogger = JiraLoggerManager.getJiraLogger();
    private JiraRestClient restClient;
    private @Getter Issue issue;
    private @Getter FieldIssue fieldIssue;

    /**
     * Конструктор класса для работы с Jira
     * Создает объект JiraRestClient для использования методов обращения к Jira
     */
    public JiraWork() {
        try{
            restClient = new CustomAsynchronousJiraRestClientFactory().createWithBasicHttpAuthenticationCustom(
                    new URI(JiraConfig.CONFIG.getUrl()),
                    JiraConfig.CONFIG.getUsername(), JiraConfig.CONFIG.getPassword(), 30000);
            logger.log(LoggerEventType.JIRA_INFO,
                    String.format("Подключение к %s успешно.", JiraConfig.CONFIG.getUrl()));
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    String.format("Не удалось подключиться к %s\nТекст ошибки: %s", JiraConfig.CONFIG.getUrl(), e.getMessage()));
        }
    }

    public List<String> getDayIssues(){
        return getFilter(JiraConfig.CONFIG.getFilterDayPlan(), 200, "Дневные");
    }

    public List<String> getNightIssues(){
        return getFilter(JiraConfig.CONFIG.getFilterNightPlan(), 200, "Ночные");
    }
    /**
     * Выгружает заявки по фильтру
     *
     * @param filterId      ID фильтра из Jira
     * @param maxResults    Максимальное числа заявок для выгрузки
     * @return              Возвращает итератор с issue key
     */
    public List<String> getFilter(Integer filterId, Integer maxResults, String time) {
        try{
            Promise<Filter> promise =  restClient.getSearchClient().getFilter(filterId);
            String jqlString = promise.claim().getJql();

            Promise<SearchResult> searchResult = restClient.getSearchClient().searchJql(jqlString, maxResults, 0, null);
            SearchResult results = searchResult.claim();
            logger.log(LoggerEventType.JIRA_INFO,
                    String.format("%s заявки по фильтру выгрузил.", time));
            return getIssuesKeys(results.getIssues());
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    String.format("Не удалось выгрузить %s заявки по фильтру\nТекст ошибки: %s", time, e.getMessage()));
            return null;
        }
    }

    /**
     * Достает из объекта Issue ключи заявок
     *
     * @param issues        Итератор объектов Issue
     * @return              Возвращает список ключей заявок
     */
    private List<String> getIssuesKeys(Iterable<Issue> issues){
        List<String> issuesKeys = new ArrayList<>();
        for(Issue i : issues)
            issuesKeys.add(i.getKey());
        return issuesKeys;
    }

    /**
     * Переводит заявку на следующий статус по БП
     *
     * @param errors    Есть ли ошибки в накате
     */
    public void switchStatus(boolean errors){
        Map<String, Integer> map;
        if(errors)
            map = JiraConfig.CONFIG.getIssueErrorSteps();
        else
            map = JiraConfig.CONFIG.getIssueSteps();
        try{
            if(JiraConfig.CONFIG.getIssueSteps().containsKey(issue.getStatus().getName())){
                restClient.getIssueClient().transition(issue, new TransitionInput(map.get(issue.getStatus().getName()))).claim();
                logger.log(LoggerEventType.JIRA_INFO,
                        issue.getKey(),
                        String.format("Перевёл на шаг: %s", map.get(issue.getStatus().getName())));
            }else{
                logger.log(LoggerEventType.JIRA_ERROR,
                        issue.getKey(),
                        String.format("Нет функционала для перевода заявки с шага %s",
                                issue.getStatus().getName()));
            }
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось перевести заявку с шага %s на шаг %s\nТекст ошибки: %s",
                            issue.getStatus().getName(), map.get(issue.getStatus().getName()), e.getMessage()));
        }
    }

    /**
     * Переводит заявку по указанному ID Transition
     *
     * @param trasitionId    Id статуса заявки
     */
    public void switchStatusById(Integer trasitionId){
        try{
            restClient.getIssueClient().transition(issue, new TransitionInput(trasitionId)).claim();
            logger.log(LoggerEventType.JIRA_INFO,
                    issue.getKey(),
                    String.format("Перевёл на шаг: %s",  trasitionId));
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось перевести заявку с шага %s на шаг %s\nТекст ошибки: %s",
                            issue.getStatus().getName(), trasitionId, e.getMessage()));
        }
    }

    /**
     * Проставляет комментарий в задаче
     *
     * @param text    Текст комментария
     */
    public void commentAdd(String text){
        try {
            restClient.getIssueClient().addComment(issue.getCommentsUri(), Comment.valueOf(text)).claim();
            logger.log(LoggerEventType.JIRA_INFO,
                    issue.getKey(),
                    "Комментарий отписал");
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось отписать комментарий: %s\nТекст ошибки: %s", text, e.getMessage()));
        }
    }

    /**
     * Выводит содержимое лога в комментарии в задаче
     *
     * @param text    Текст комментария
     */
    public void commentLog(String text){
        try {
            if(!text.isEmpty()){
                commentAdd(text);
                logger.log(LoggerEventType.JIRA_INFO,
                        issue.getKey(),
                        "Комментарий из JiraLog отписал");
            }else{
                logger.log(LoggerEventType.JIRA_INFO,
                        issue.getKey(),
                        "Лог Jira пустой.");
            }
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось отписать комментарий из JiraLog: %s\nТекст ошибки: %s", text, e.getMessage()));
        }
    }

    /**
     * Проставляет значение в нужное поле
     *
     * @param fieldId   Id поля в Jira
     * @param setValue  Значение которое нужно вставить
     */
    private void setFieldValue(String fieldId, String setValue) {
        try{
            IssueInputBuilder issueInputBuilder = new IssueInputBuilder();
            Map<String, Object> mapValues = new HashMap<String, Object>();
            mapValues.put("value", setValue);
            ComplexIssueInputFieldValue value = new ComplexIssueInputFieldValue(mapValues);
            issueInputBuilder.setFieldValue(fieldId, value);
            IssueInput issueInput = issueInputBuilder.build();
            restClient.getIssueClient().updateIssue(issue.getKey(), issueInput).get();
            logger.log(LoggerEventType.JIRA_INFO,
                    issue.getKey(),
                    String.format("Значение %s в поле %s проставил.", setValue, fieldId));
        }catch(Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Ошибка в проставлении значения в поле: %s.\nТекст ошибки: %s", fieldId, e.getMessage()));
        }
    }

    /**
     * Проставляет прокаченные города
     *
     * @param valuesList Список прокаченных городов
     */
    public void setDeployedCities(List<String> valuesList) {
        try{
            List<ComplexIssueInputFieldValue> fieldList = new ArrayList<ComplexIssueInputFieldValue>();
            valuesList.addAll(fieldIssue.getAlreadyDeployedCities());
            for (String value : valuesList){
                Map<String, Object> mapValues = new HashMap<String, Object>();
                mapValues.put("value", value);
                ComplexIssueInputFieldValue fieldValue = new ComplexIssueInputFieldValue(mapValues);
                fieldList.add(fieldValue);
            }
            IssueInputBuilder issueInputBuilder = new IssueInputBuilder();
            issueInputBuilder.setFieldValue(JiraConfig.CONFIG.getDeployedCityId(), fieldList);
            IssueInput issueInput = issueInputBuilder.build();
            restClient.getIssueClient().updateIssue(issue.getKey(), issueInput).get();
            logger.log(LoggerEventType.JIRA_INFO,
                    issue.getKey(),
                    "Прокаченные города проставил.");
        }catch (Exception e) {
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Ошибка в проставлении прокаченных городов.\nТекст ошибки: %s", e.getMessage()));
        }
    }

    /**
     * Проверка оформления заявки
     *
     * @return Возвращает результат проверки заявки
     */
    public boolean checkFieldIssue() {
        try{
            String negativeResult = "h3. Заявка не принята в накат\n";
            jiraLogger.append(negativeResult);
            boolean result = true;
            // Проверка компонент
            int countComponents = 0;
            String currentComponent = "";
            for(String component : fieldIssue.getComponents()){
                if(JiraConfig.CONFIG.getComponentsFields().contains(component)){
                    countComponents++;
                    currentComponent = component;
                }
            }
            // Проверка наличия компонент
            if(countComponents == 0){
                result = false;
                logger.log(LoggerEventType.JIRA_INFO,
                        issue.getKey(),
                        "Не указана компонента для наката.");
                jiraLogger.append("* Необходимо добавить одну из компонент:\n'Накат. Исправление ошибки', 'Накат. Новый функционал', 'Накат. Оптимизация', 'Накат. групповая операция'.\nhttps://kb.ertelecom.ru/pages/viewpage.action?pageId=128457345\n");
            }

            // Проверка что в заявке не более одной компоненты
            if(countComponents > 1){
                result = false;
                logger.log(LoggerEventType.JIRA_INFO,
                        issue.getKey(),
                        "Указано более одной компоненты для наката.");
                jiraLogger.append("* Указано более одной компоненты, определяющей тип заявки.\nОставьте только одну из компонент: 'Накат. Исправление ошибки', 'Накат. Новый функционал', 'Накат. Оптимизация', 'Накат. групповая операция'.\nhttps://kb.ertelecom.ru/pages/viewpage.action?pageId=128457345\n");
            }

            //проверим описание
            if (fieldIssue.getDescription() == null)
            {
                logger.log(LoggerEventType.JIRA_INFO,
                        issue.getKey(),
                        "У заявки нет описания.");
                jiraLogger.append("* У заявки нет описания.\n");
            }

            // Для исправления ошибок смотрим поле виновники
            if (currentComponent.equals(JiraConfig.CONFIG.getFixBugs()) && fieldIssue.getGuiltyGroup() == null)
            {
                result = false;
                logger.log(LoggerEventType.JIRA_INFO,
                        issue.getKey(),
                        "Не заполнено поле Виновники.");
                jiraLogger.append("* Не заполнено поле Виновники.\n");
            }

            // Проверки для нового функционала
            if (currentComponent.equals(JiraConfig.CONFIG.getNewFunctional()))
            {
                // Предметная область
                if (fieldIssue.getSubjectArea() == null)
                {
                    result = false;
                    logger.log(LoggerEventType.JIRA_INFO,
                            issue.getKey(),
                            "Не заполнено поле Предметная область.");
                    jiraLogger.append("* Не заполнено поле Предметная область.\n");
                }
                // Заинтересованная группа
                if (fieldIssue.getTargetGroup() == null)
                {
                    result = false;
                    logger.log(LoggerEventType.JIRA_INFO,
                            issue.getKey(),
                            "Не заполнено поле Заинтересованная группа.");
                    jiraLogger.append("* Не заполнено поле Заинтересованная группа.\n");
                }
                // Риски
                if (fieldIssue.getRisks() == null)
                {
                    result = false;
                    logger.log(LoggerEventType.JIRA_INFO,
                            issue.getKey(),
                            "Не заполнено поле Риски.");
                    jiraLogger.append("* Не заполнено поле Риски.\n");
                }
            }

            // Проверки для групповых операций (НАДО ДОПИЛИТЬ, НУЖНЫ ПРОВЕРКИ НА LOOP В НАКАТНЫХ ФАЙЛАХ
            if(issue.getStatus().getName().equals("Ожидание наката")){
                boolean loop = false;
                boolean hasOci = false;

                if (!currentComponent.equals(JiraConfig.CONFIG.getGroupOperation()) && loop) {
                    result = false;
                    logger.log(LoggerEventType.JIRA_INFO,
                            issue.getKey(),
                            "В заявке есть групповая операция, но не выставлена компонента 'Накат. Групповая операция'");
                    jiraLogger.append("* В заявке есть групповая операция.\n Пожалуйста, смените компоненту на 'Накат. Групповая операция'\n");
                }
                if (loop && hasOci) {
                    result = false;
                    logger.log(LoggerEventType.JIRA_INFO,
                            issue.getKey(),
                            "Групповая операция и накат пакетов в одной заявке");
                    jiraLogger.append("* Нельзя выполнять групповую операцию и вносить изменения в пакет в одной заявке.\n Пожалуйста, разделите заявки.\n");
                }
            }

            // Проверка тяжелых пакетов
            List<String> affectedObject = new FieldIssue(issue).getAffectedObject();
            if(affectedObject != null){
                StringBuilder objectToLog = new StringBuilder("");
                for(String heavyPackage: HeavyPackages.CONFIG.getHeavyPackages()){
                    if(affectedObject.contains(heavyPackage)){
                        setFieldValue(JiraConfig.CONFIG.getHeavyId(), "Да");
                        objectToLog.append(heavyPackage).append("\n");
                    }
                }
                if(!objectToLog.toString().isEmpty()){
                    result = false;
                    logger.log(LoggerEventType.JIRA_INFO,
                            issue.getKey(),
                            "Заявка содержит тяжелые пакеты.");
                    jiraLogger.append(String.format("* Заявка содержит тяжелые пакеты: %s\n", objectToLog.toString()));
                }
            }

            // проверка пулл-реквестов
            if(!checkPullRequest())
                result = false;

            // если проверка прошла успешно, удалим из лога строку "Заявка не принята в накат"
            if(result){
                Integer firstIndex = jiraLogger.getFullMessage().indexOf(negativeResult);
                Integer lastIndex = negativeResult.length();
                jiraLogger.delete(firstIndex, lastIndex);
            }

            return result;
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось проверить заявку.\nТекст ошибки: %s", e.getMessage()));
            return false;
        }
    }

    /**
     * Проверка открытых и несмерженных пулл-реквестов
     *
     * @return Возвращает результат проверки
     */
    private boolean checkPullRequest(){
        try{
            if (getFieldPullRequest("openCount") != 0){
                logger.log(LoggerEventType.JIRA_INFO,
                        issue.getKey(),
                        "В заявке есть открытые пулл-реквесты.");
                jiraLogger.append("* В заявке есть открытые пулл-реквесты.\n");
                return false;
            }
            if (getFieldPullRequest("mergedCount") == 0){
                logger.log(LoggerEventType.JIRA_INFO,
                        issue.getKey(),
                        "В заявке нет смерженных пулл-реквестов.");
                jiraLogger.append("* В заявке нет смерженных пулл-реквестов.\n");
                return false;
            }
            return true;
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    "Ошибка при проверке пулл-реквестов.");
            return false;
        }
    }

    /**
     * Получает информацию по пулл реквесту
     *
     * @return Возвращает значение нужного объекта
     */
    private Integer getFieldPullRequest(String object){
        try{
            int result = -1;
            String text = new FieldIssue(issue).getPullRequestInfo();
            Pattern pattern = Pattern.compile("\"details\":.+?}");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String str = "{" + text.substring(matcher.start(), matcher.end()) + "}";
                result = Integer.parseInt(new JSONObject(new JSONObject(str).get("details").toString()).get(object).toString());
            }
            return result;
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    "Ошибка выгрузки значений по пулл-реквестам.");
            return null;
        }
    }

    /**
     * Проверка закрыта ли заявка
     *
     * @return Возвращает true/false
     */
    public boolean checkCloseIssue(){
        try{
            return issue.getStatus().getName().equals("Закрыта");
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    "Ошибка проверки статуса \"Закрыта\".");
            return false;
        }
    }

    /**
     * Установить значения объектам Issue и CustomFieldIssue
     *
     * @param issueId   Ключ задачи
     */
    public void setIssue(String issueId) {
        try{
            this.issue = restClient.getIssueClient().getIssue(issueId).get();
            this.fieldIssue = new FieldIssue(this.issue);
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Ошибка создания объекта Issue\nТекст ошибки: %s", e.getMessage()));
        }

    }
}