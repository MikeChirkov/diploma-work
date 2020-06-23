package ru.ertelecom.logger;

import ru.ertelecom.postgres.EventsQuery;
import ru.ertelecom.postgres.PostgresConnector;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс для логирования с номером заявки
 */
public class IssueLog extends BaseLog {

    private String issueKey;

    /**
     * Конструктор класса логирования с номером задач
     */
    public IssueLog(PostgresConnector postgreSQL, String className, String eventType, String message,
                    String issueKey){
        super(postgreSQL, className, eventType, message);
        this.issueKey = issueKey;
    }

    /**
     * Метод записывающий сообщения в консоль
     */
    public void writeToConsole(){
        Date dateNow = new Date();

        SimpleDateFormat formatDateConsole = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        String dateConsole = formatDateConsole.format(dateNow);
        String eventTypeConsole = String.format("[%s]", eventType);
        String issueKeyConsole = String.format("[%s]", issueKey);
        String classNameConsole =  String.format("[%s]", className);

        String consoleOut = dateConsole + " - " +
                eventTypeConsole + " - " +
                classNameConsole + " - " +
                issueKeyConsole + " - " +
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
        String issueKeyDb = String.format("'%s'", issueKey);

        String insertIntoEvents = "INSERT INTO event" +
                " (event_date, event_type_id, class, issue_id, message) " +
                "VALUES('" + dateDb +"'," + getEventTypeId(eventTypeDb) +",'"
                + className + "'," + getIssueId(issueKeyDb) + ",'" + message + "');";
        new EventsQuery().insertEvents(insertIntoEvents, postgreSQL);
    }
}
