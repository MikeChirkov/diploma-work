package ru.ertelecom.logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для создания объектов Logger
 */
public class LoggerManager {
    private static List<Logger> loggerInstances;

    public LoggerManager(){
    }


    /**
     * Метод, который достает Logger из списка Logger-ов
     *
     * @param classObj     Объект класса
     *
     * @return  Возвращает объект Logger
     */
    public static Logger getLogger(Class classObj) {
        if (loggerInstances == null) {
            loggerInstances = new ArrayList<Logger>();
            Logger loggerNew = new Logger(classObj);
            loggerInstances.add(loggerNew);
            return loggerNew;
        }else{
            Logger logger = getLoggerByClassName(classObj.getName());
            if(logger != null){
                return logger;
            }else{
                Logger loggerNew = new Logger(classObj);
                loggerInstances.add(loggerNew);
                return loggerNew;
            }
        }
    }

    /**
     * Метод, который достает Logger по имени класса
     *
     * @param className     Имя класса
     *
     * @return  Возвращает объект Logger
     */
    private static Logger getLoggerByClassName(String className){
        for(Logger logger : loggerInstances){
            if(logger.getClassName().equals(className)){
                return logger;
            }
        }
        return null;
    }
}
