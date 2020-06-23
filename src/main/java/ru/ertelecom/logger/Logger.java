package ru.ertelecom.logger;

import lombok.Getter;
import lombok.Setter;
import ru.ertelecom.postgres.PostgresConnector;

/**
 * Самописный класс для различного вида логирования
 */
public class Logger {

    private @Getter @Setter String className;
    private PostgresConnector postgreSQL;

    /**
     * Конструктор класса логирования
     * Достает имя класса в котором необходимо логирование
     * И подключается к БД Postgres с логами
     */
    public Logger(Class classObj){
        className = classObj.getName();
        postgreSQL = new PostgresConnector();
        postgreSQL.connect();
    }

    /**
     * Метод для обычного логирования сообщения, без указания задачи и города
     *
     * @param eventType     Тип события
     * @param message       Сообщение в лог
     */
    public void log(LoggerEventType eventType, String message) {
        BaseLog baseLog = new BaseLog(postgreSQL, className,
                eventType.toString(), message);
        baseLog.writeToConsole();
        baseLog.writeToDataBase();
    }

    /**
     * Метод для логирования сообщения и номера задачи, без указания города
     *
     * @param eventType     Тип события
     * @param issueKey      Номер заявки
     * @param message       Сообщение в лог
     */
    public void log(LoggerEventType eventType, String issueKey, String message) {
        IssueLog issueLog = new IssueLog(postgreSQL, className,
                eventType.toString(), message, issueKey);
        issueLog.writeToConsole();
        issueLog.writeToDataBase();
    }

    /**
     * Метод для логирования сообщения, номера задачи и города
     *
     * @param eventType     Тип события
     * @param issueKey      Номер заявки
     * @param cityDeploy    Название города
     * @param message       Сообщение в лог
     */
    public void log(LoggerEventType eventType, String issueKey, String cityDeploy, String message) {
        IssueCityLog issueCityLog = new IssueCityLog(postgreSQL, className,
                eventType.toString(), message, issueKey, cityDeploy);
        issueCityLog.writeToConsole();
        issueCityLog.writeToDataBase();
    }

}

