package ru.ertelecom.logger;

public class JiraLoggerManager {
    private static JiraLogger jiraLogger;

    public static JiraLogger getJiraLogger() {
        if(jiraLogger == null)
            jiraLogger = new JiraLogger();
        return jiraLogger;
    }
}