package ru.ertelecom;


import ru.ertelecom.attachedfiles.AttachedFile;
import ru.ertelecom.attachedfiles.AttachedFileFactory;
import ru.ertelecom.attachedfiles.objects.PackageAttached;
import ru.ertelecom.attachedfiles.objects.SqlPlusAttached;
import ru.ertelecom.attachedfiles.objects.WebAttached;
import ru.ertelecom.config.JiraConfig;
import ru.ertelecom.config.MainConfig;
import ru.ertelecom.git.GitWork;
import ru.ertelecom.jira.JiraWork;
import ru.ertelecom.logger.JiraLogger;
import ru.ertelecom.logger.JiraLoggerManager;
import ru.ertelecom.logger.Logger;
import ru.ertelecom.logger.LoggerManager;
import ru.ertelecom.shellexecute.Shell;

import java.io.*;
import java.util.*;

public class MainClass {
    public static void main(String[] args){

        // Пример вызова консольного приложения jdeployer
        // Тест накатных файлов
        testAttachedFiles();
        // Тест Jira и Git
        //testJiraAndGit();
    }

    //  Связать Jira и JDeployServer
    //  Подготовка обработки и передачи заявки
    private static void testAttachedFiles(){

        // Выбираем заявку по которой достаем накатный файл
        String issueKey = "DEV-79201";//84416 83654 83993

        // Достаем путь к накатному файлу
        String pathToAttach = MainConfig.CONFIG.getDeployRepoPath().replace(".git", "") + issueKey;

        // Достаем каталог с файлами
        File myFolder = new File(pathToAttach);
        // Достаем массив файлов из каталога
        File[] files = myFolder.listFiles();
        // Список для вывода ниже
        List<Object> attachedFiles = new ArrayList<>();
        assert files != null;
        // Пробегаем файлы и достаем из них объекты для наката
        for(File file : files){
            // Достаем объект накатного файла
            AttachedFile attachedFile = AttachedFileFactory.getAttachedFile(issueKey + "/" + file.getName());
            // Если он не пустой и его можно катать
            if(attachedFile != null && attachedFile.getCanDeploy()){
                // Достаем из него список объектов
                List<Object> objectList = attachedFile.getAttachedContents();

                if(objectList != null && attachedFile.getCanDeploy()){
                    attachedFiles.addAll(objectList);
                }
            }
        }
        // Вывод информации по объектам Package SqlPlus и Web
        for(Object object : attachedFiles){
            System.out.println();
            System.out.println();
            if(object instanceof PackageAttached){
                System.out.println(((PackageAttached) object).getServer() + " " + ((PackageAttached) object).getSchema());
                System.out.println("only_for: " + ((PackageAttached) object).getOnlyFor() + "; exclude: " + ((PackageAttached) object).getExclude());
                System.out.println("Spec: " + ((PackageAttached) object).isDeploySpec() + "; Body: " + ((PackageAttached) object).isDeployBody());
                System.out.println(((PackageAttached) object).getPathToFile() + " " + ((PackageAttached) object).getRevision());
                //System.out.println(((Package) object).getSpec());
                //System.out.println(((Package) object).getBody());
                //System.out.println(((PackageAttached) object).getSourceCode());
            }else if(object instanceof SqlPlusAttached){
                System.out.println(((SqlPlusAttached) object).getServer() + " " + ((SqlPlusAttached) object).getSchema());
                System.out.println("only_for: " + ((SqlPlusAttached) object).getOnlyFor() + "; exclude: " + ((SqlPlusAttached) object).getExclude());
                System.out.println(((SqlPlusAttached) object).getPathToFile() + "; crit: " + ((SqlPlusAttached) object).isCrit());
                //System.out.println(((SqlPlus) object).getSourceCode());
            }else if(object instanceof WebAttached){
                System.out.println(((WebAttached) object).getPathToFile() + " " + ((WebAttached) object).getRevision());
                System.out.println(((WebAttached) object).getSourceCode());
            }
        }
        JiraLogger jiraLogger = JiraLoggerManager.getJiraLogger();
        JiraWork jiraWork = new JiraWork();
        jiraWork.setIssue("DEV-79201");
        jiraWork.commentLog(jiraLogger.getFullMessage());
    }

    private static void testJiraAndGit(){

        JiraLogger jiraLogger = JiraLoggerManager.getJiraLogger();
        // пример обновления репозиториев
        GitWork gitDeploy = new GitWork(MainConfig.CONFIG.getDeployRepoPath());
        //gitDeploy.gitUpdateRepository();
        GitWork gitEngine = new GitWork(MainConfig.CONFIG.getEngineRepoPath());
        //gitEngine.gitUpdateRepository();
        GitWork gitWww = new GitWork(MainConfig.CONFIG.getWwwDataRepoPath());
        //gitWww.gitUpdateRepository();

        // пример переключения на ветки и тэг
        gitWww.gitCheckoutBranch("master");
        gitWww.gitCheckoutTag("8123");

        // пример как достать нужную ревизию по файлу
        String sourceGit = gitWww.gitGetSourceCode("3456789", "data/2.js");
        //System.out.println(sourceGit);

        // Создаем объект для работы с Jira
        JiraWork jiraWork = new JiraWork();
        // Пример выгрузки по фильтрам
        System.out.println();
        List<String> results = jiraWork.getNightIssues();
        // Перебор и вывод информации по задачам из фильтра
        for (String issueKey : results) {
            jiraWork.setIssue(issueKey);
            System.out.println("-----");
            System.out.println("Заявка: " + jiraWork.getFieldIssue().getKey());
            System.out.println("Компоненты: " + jiraWork.getFieldIssue().getComponents());
            System.out.println("Статус: " + jiraWork.getFieldIssue().getStatus());
            System.out.println("Статус JiraID: " + jiraWork.getFieldIssue().getStatusJiraId());
            System.out.println("Приоритет: " + jiraWork.getFieldIssue().getPriority());
            System.out.println("Приоритет JiraID: " + jiraWork.getFieldIssue().getPriorityJiraId());
            System.out.println("Исполнитель: " + jiraWork.getFieldIssue().getAssignee());
            System.out.println("Автор: " + jiraWork.getFieldIssue().getReporter());
            System.out.println("БТ: " + jiraWork.getFieldIssue().getBusinesTechnologist());
            System.out.println("Код-ревьюер: " + jiraWork.getFieldIssue().getCodeReviewer());
            System.out.println("Заказчик изменений: " + jiraWork.getFieldIssue().getChangeCustomer());
            System.out.println("Сервер: " + jiraWork.getFieldIssue().getServer());
            System.out.println("Тип заявки: " + jiraWork.getFieldIssue().getType());
            System.out.println("Тип заявки JiraID: " + jiraWork.getFieldIssue().getTypeJiraId());
            System.out.println("Тяжелый накат: " + jiraWork.getFieldIssue().getHeavy());
            System.out.println("Первый город: " + jiraWork.getFieldIssue().getFirstCity());
            System.out.println("Второй город: " + jiraWork.getFieldIssue().getSecondCity());
            System.out.println("Прокаченные: " + jiraWork.getFieldIssue().getDeployedCity());
            System.out.println("Согласователи: " + jiraWork.getFieldIssue().getCoordinators());

            System.out.println("Риски: " + jiraWork.getFieldIssue().getRisks());
            System.out.println("Заинт. группа: " + jiraWork.getFieldIssue().getTargetGroup());
            System.out.println("Виновники: " + jiraWork.getFieldIssue().getGuiltyGroup());
            System.out.println("Предметная область: " + jiraWork.getFieldIssue().getSubjectArea());
            System.out.println("Затронутые объекты: " + jiraWork.getFieldIssue().getAffectedObject());

            System.out.println("Описание: " + jiraWork.getFieldIssue().getDescription());
            System.out.println("Отклонение от регламента: " + jiraWork.getFieldIssue().getDeviationFromRegulation());
            System.out.println("Дата внерега: " + jiraWork.getFieldIssue().getDateDeviationFromRegulation());

            System.out.println("Сделать после: " + jiraWork.getFieldIssue().getDoAfter());
            System.out.println("Сделать перед: " + jiraWork.getFieldIssue().getDoBefore());

            System.out.println("Метки: " + jiraWork.getFieldIssue().getLabels());
            System.out.println("Связи проставлены: " + jiraWork.getFieldIssue().getLinksIsMarked());
        }

        // Пример для работы с одиночной задачей
        jiraWork.setIssue("DEV-84264");

        // Допустим прокатили эти города:
        List<String> list = new ArrayList<>();
        list.add("Пермь");
        list.add("Москва");
        list.add("Казань");
        list.add("Новосибирск");

        // Установим значение в прокаченные города
        jiraWork.setDeployedCities(list);

        // Проверка заявки на оформление
        boolean flag = jiraWork.checkFieldIssue();
        if(!flag)
            jiraWork.switchStatus(true); // Переключить статус если были ошибки

        // Переключить статус заявки по Id
        // 1151 - Уведомление одного города, см. конфиг
        // Логика в конфиге следующая: Есть накат на один город, рядом стоит ID СЛЕДУЮЩЕГО шага по БП,
        // т.е. уведомление двух городов, либо ошибка 1 город
        jiraWork.switchStatusById(JiraConfig.CONFIG.getErrorTest());

        // Проверка закрыта ли заявка
        System.out.println(jiraWork.checkCloseIssue());
        // Отписать в комментарии лог jira (пока что там проверка на оформление)
        jiraWork.commentLog(jiraLogger.getFullMessage());
    }
}