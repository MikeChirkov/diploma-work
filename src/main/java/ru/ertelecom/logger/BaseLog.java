package ru.ertelecom.logger;

import lombok.Getter;
import lombok.Setter;
import ru.ertelecom.postgres.EventsQuery;
import ru.ertelecom.postgres.PostgresConnector;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Базовый класс для логирования дата + тип события + класс в котором произошло событие + сообщение
 */
public class КBaseLog {
    protected  @Getter @Setter String className;
    protected PostgresConnector postgreSQL;
    protected String eventType;
    protected String message;

    /**
     * Конструктор базового класса для логирования
     */
    public BaseLog(PostgresConnector postgreSQL, String className, String eventType, String message){
        this.postgreSQL = postgreSQL;
        this.className = className;
        this.eventType = eventType;
        this.message = message;
    }

    /**
     * Метод записывающий сообщения в консоль
     */
    public void writeToConsole(){
        Date dateNow = new Date();
        SimpleDateFormat formatDateConsole = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        String dateConsole = formatDateConsole.format(dateNow);
        String eventTypeConsole = String.format("[%s]", eventType);
        String classNameConsole =  String.format("[%s]", className);

        String consoleOut = dateConsole + " - " +
                eventTypeConsole + " - " +
                classNameConsole + " - " +
                message;

        System.out.println(consoleOut);
    }

    /**
     * Метод записывающий сообщения в бд
     */
    public void writeToDataBase(){
        Date dateNow = new Date();
        SimpleDateFormat formatDateDb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateDb = formatDateDb.format(dateNow);
        String eventTypeDb= String.format("'%s'", eventType);

        String insertIntoEvents = "INSERT INTO event" +
                " (event_date, event_type_id, class, message) " +
                "VALUES('" + dateDb + "', " + getEventTypeId(eventTypeDb)
                + ", '" + className + "', '" + message + "');";
        new EventsQuery().insertEvents(insertIntoEvents, postgreSQL);
    }

    /**
     * Метод для получения идентификатора типа события
     *
     * @param eventTypeDb Тип события из ENUM LoggerEventType
     * @return Идентификатор типа события из таблицы event_types
     */
    protected Integer getEventTypeId(String eventTypeDb){
        String selectTableEventTypes = "SELECT event_type_id from event_type where name = " + eventTypeDb;
        return new EventsQuery().execute(selectTableEventTypes, "event_type_id", postgreSQL);
    }

    /**
     * Метод для получения идентификатора заявки
     *
     * @param issueKeyDb Ключ заявки
     * @return Идентификатор заяви из таблицы issue
     */
    protected Integer getIssueId(String issueKeyDb){
        String selectTableIssue = "SELECT issue_id from issue where number_issue = " + issueKeyDb;
        return new EventsQuery().execute(selectTableIssue, "issue_id", postgreSQL);
    }

}
