package ru.ertelecom.jira;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueLink;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.ertelecom.config.JiraConfig;
import ru.ertelecom.logger.Logger;
import ru.ertelecom.logger.LoggerEventType;
import ru.ertelecom.logger.LoggerManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Класс для работы с кастомными полями Jira
 */
public class FieldIssue {

    private static Logger logger = LoggerManager.getLogger(FieldIssue.class);
    private Issue issue;
    public FieldIssue(Issue issue){
        this.issue = issue;
    }

    /**
     * @return Возвращает Ключ заявки
     */
    public String getKey(){
        return issue.getKey();
    }

    /**
     * @return Возвращает Статус заявки
     */
    public String getStatus(){
        return issue.getStatus().getName();
    }

    /**
     * @return Возвращает JiraID статуса заявки
     */
    public String getStatusJiraId(){
        return issue.getStatus().getId().toString();
    }

    /**
     * @return Возвращает значения поля: Приоритет заявки
     */
    public String getPriority(){
        return Objects.requireNonNull(issue.getPriority()).getName();
    }

    /**
     * @return Возвращает JiraID приоритета заявки
     */
    public String getPriorityJiraId(){
        return Objects.requireNonNull(Objects.requireNonNull(issue.getPriority()).getId()).toString();
    }

    /**
     * @return Возвращает значения поля: Автор
     */
    public String getReporter(){
        return Objects.requireNonNull(issue.getReporter()).getName();
    }

    /**
     * @return Возвращает значения поля: Исполнитель
     */
    public String getAssignee(){
        return Objects.requireNonNull(issue.getAssignee()).getName();
    }

    /**
     * @return Возвращает значения поля: Бизнес-технолог
     */
    public String getBusinesTechnologist(){
        return getStringFromObject(JiraConfig.CONFIG.getBusinesTechnologistId(), "name");
    }

    /**
     * @return Возвращает значения поля: Заказчик изменений
     */
    public String getChangeCustomer(){
        return getStringFromObject(JiraConfig.CONFIG.getChangeCustomerId(), "name");
    }

    /**
     * @return Возвращает значения поля: Код-ревьюер
     */
    public String getCodeReviewer(){
        return getStringFromObject(JiraConfig.CONFIG.getCodeReviewerId(), "name");
    }

    /**
     * @return Возвращает значения поля: Описание
     */
    public String getDescription(){
        return issue.getDescription();
    }

    /**
     * @return Возвращает значения поля: Сервер
     */
    public String getServer(){
        return getStringFromObject(JiraConfig.CONFIG.getServerId(), "value");
    }

    /**
     * @return Возвращает значения поля: Тип наката
     */
    public String getType(){
        return getIdFromObject(JiraConfig.CONFIG.getTypeId(), "value");
    }

    /**
     * @return Возвращает JiraID поля: Тип наката
     */
    public String getTypeJiraId(){
        return getIdFromObject(JiraConfig.CONFIG.getTypeId(), "id");
    }

    /**
     * @return Возвращает значения поля: Тяжелый накат
     */
    public String getHeavy(){
        return getStringFromObject(JiraConfig.CONFIG.getHeavyId(), "value");
    }

    /**
     * @return Возвращает значения поля: Согласователи
     */
    public List<String> getCoordinators(){
        return getListFromArray(JiraConfig.CONFIG.getCoordinatorsId(), "name");
    }

    /**
     * @return Возвращает значения поля: Первый город наката
     */
    public List<String> getFirstCity(){
        return getListFromArray(JiraConfig.CONFIG.getFirstCityId(), "value");
    }

    /**
     * @return Возвращает значения поля: Второй и третий города наката
     */
    public List<String> getSecondCity(){
        return getListFromArray(JiraConfig.CONFIG.getSecondCityId(), "value");
    }

    /**
     * @return Возвращает значения поля: Прокаченные города
     */
    public List<String> getDeployedCity(){
        return getListFromArray(JiraConfig.CONFIG.getDeployedCityId(), "value");
    }

    /**
     * @return Возвращает значения поля: Виновники
     */
    public List<String> getGuiltyGroup(){
        return getListFromArray(JiraConfig.CONFIG.getGuiltyGroupId(), "name");
    }

    /**
     * @return Возвращает значения поля: Предметная область
     */
    public String getSubjectArea(){
        return getValueFromIssue(JiraConfig.CONFIG.getSubjectAreaId());
    }

    /**
     * @return Возвращает значения поля: Заинтересованная группа
     */
    public String getTargetGroup(){
        return getValueFromIssue(JiraConfig.CONFIG.getTargetGroupId());
    }

    /**
     * @return Возвращает информацию по пулл реквестам
     */
    public String getPullRequestInfo(){
        return getValueFromIssue(JiraConfig.CONFIG.getPullRequestInfoId());
    }

    /**
     * @return Возвращает значение поля: Связи проставлены
     */
    public String getLinksIsMarked(){
        return getStringFromObject(JiraConfig.CONFIG.getLinksIsMarked(), "value");
    }

    /**
     * @return Возвращает значения поля: Затронутые объекты
     */
    public List<String> getAffectedObject(){
        try{
            String object = getValueFromIssue(JiraConfig.CONFIG.getAffectedObjectId());
            assert object != null;
            String[] objectSplit = object.split("\n");
            return new ArrayList<String>(Arrays.asList(objectSplit));
        }catch (NullPointerException e){
            return null;
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось выгрузить затронутые объекты по заявке\nТекст ошибки: %s",  e.getMessage()));
            return null;
        }
    }

    /**
     * @return Возвращает значения поля: Компоненты
     */
    public List<String> getComponents(){
        try{
            List<String> components = new ArrayList<String>();
            for(BasicComponent bc : issue.getComponents()){
                components.add(bc.getName());
            }
            return components;
        }catch (NullPointerException e){
            return null;
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось выгрузить компоненты по заявке.\nТекст ошибки: %s",  e.getMessage()));
            return null;
        }
    }

    /**
     * @return Возвращает значения поля: Метки
     */
    public List<String> getLabels(){
        try{
            return new ArrayList<String>(issue.getLabels());
        }catch (NullPointerException e){
            return null;
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось выгрузить метки из заявки.\nТекст ошибки: %s",  e.getMessage()));
            return null;
        }
    }

    /**
     * Достает связные задачи сделать после
     *
     * @return        Возвращает список ключей заявок
     */
    public List<String> getDoAfter(){
        return getNeedLink(JiraConfig.CONFIG.getDoAfter());
    }

    /**
     * Достает связные задачи сделать перед
     *
     * @return      Возвращает строку в виде JSON массива
     */
    public List<String> getDoBefore(){
        return getNeedLink(JiraConfig.CONFIG.getDoBefore());
    }

    /**
     * Достает нужную связь
     *
     * @param link  Название связи
     * @return      Возвращает List с задачами по нужной связи
     */
    private List<String> getNeedLink(String link){
        try{
            List<IssueLink> issueLinks = getListLinks(Objects.requireNonNull(issue.getIssueLinks()), link);
            List<String> links = new ArrayList<>();
            for (IssueLink issueLink : issueLinks) {
                links.add(issueLink.getTargetIssueKey());
            }
            return links;
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось выгрузить связи из заявки.\nТекст ошибки: %s",  e.getMessage()));
            return null;
        }
    }

    /**
     * Помещает IssueLink объекты итератора в список
     *
     * @param issueLinks    Итератор объектов IssueLinks
     * @param link          Название связи
     * @return              Возвращает список ключей заявок
     */
    private List<IssueLink> getListLinks(Iterable<IssueLink> issueLinks, String link){
        List<IssueLink> result = new ArrayList<>();
        for(IssueLink l : issueLinks){
            if(l.getIssueLinkType().getDescription().equals(link))
                result.add(l);
        }
        return result;
    }

    /**
     * Получает города из поля прокаченные города в заявке, т.е. уже прокаченные
     * Необходимо для добавления городов к прокаченным деплоером
     *
     * @return Возвращает список прокаченных городов в заявке
     */
    public List<String> getAlreadyDeployedCities(){
        try{
            List<String> deployedCities = new ArrayList<String>();
            JSONArray array = new JSONArray(new FieldIssue(issue).getDeployedCity());
            for(Object obj : array){
                deployedCities.add(obj.toString());
            }
            return deployedCities;
        }catch (NullPointerException e){
            return new ArrayList<String>();
        }
    }

    /**
     * Позволяет достать значение(value) из кастомного поля
     *
     * @return Возвращает значения поля
     */
    private String getValueFromIssue(String fieldId){
        try{
            return Objects.requireNonNull(issue.getField(fieldId)).getValue().toString();
        }catch (NullPointerException e){
            return null;
        } catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось выгрузить значение поля: %s\nТекст ошибки: %s", fieldId, e.getMessage()));
            return null;
        }
    }

    /**
     * Получает строку из объекта кастомного поля
     *
     * @return Возвращает значения поля
     */
    private String getStringFromObject(String fieldId, String objectId){
        try{
            return new JSONObject(Objects.requireNonNull(issue.getField(fieldId)).getValue().toString()).get(objectId).toString();
        }catch (NullPointerException e){
            return null;
        } catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось выгрузить значение поля: %s\nТекст ошибки: %s", fieldId, e.getMessage()));
            return null;
        }
    }

    /**
     * Получает id из объекта кастомного поля
     *
     * @return Возвращает значения поля
     */
    private String getIdFromObject(String fieldId, String objectId){
        try{
            return new JSONObject(Objects.requireNonNull(issue.getField(fieldId)).getValue().toString()).get(objectId).toString();
        }catch (NullPointerException e){
            return null;
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось выгрузить значение поля: %s\nТекст ошибки: %s", fieldId, e.getMessage()));
            return null;
        }
    }

    /**
     * Получает List из массива кастомного поля
     *
     * @return Возвращает массив JSON в типе List
     */
    private List<String> getListFromArray(String fieldId, String objectId){
        try{
            JSONArray jsonArray = new JSONArray(Objects.requireNonNull(issue.getField(fieldId)).getValue().toString());
            List<String> result = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getJSONObject(i).get(objectId).toString());
            }
            return result;
        }catch (NullPointerException e){
            return null;
        }catch (Exception e){
            logger.log(LoggerEventType.JIRA_ERROR,
                    issue.getKey(),
                    String.format("Не удалось выгрузить значение поля: %s\nТекст ошибки: %s", fieldId, e.getMessage()));
            return null;
        }
    }
}
