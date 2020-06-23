package ru.ertelecom.attachedfiles;

import ru.ertelecom.attachedfiles.objects.WebAttached;
import ru.ertelecom.config.MainConfig;
import ru.ertelecom.git.GitWork;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы с накатными файлами типа .web
 */
public class WebAttachedFile implements AttachedFile{
    //  Map объктов наката данного файла
    private List<Object> attachedContents = new ArrayList<>();

    private String file;
    private String fileRevision;
    private String sourceCodeRevision;
    private boolean canDeploy = true;

    /**
     * Конструктор класса WebAttachedFile
     *
     * @param file  Путь к накатному файлу DEV-<номер задачи>/<имя файла>
     */
    public WebAttachedFile(String file) {
        this.file = file;
        fileRevision = getFileRevision();
        sourceCodeRevision = getSourceCodeFromDeploy();
    }

    /**
     * Возвращает исходный код файла c ревизией из репозитория Deploy
     *
     * @return  Возвращает исходный код накатного файла
     */
    @Override
    public String getSourceCodeFromDeploy() {
        return new GitWork(MainConfig.CONFIG.getDeployRepoPath()).gitGetSourceCode("master",  fileRevision)
                .replace(" ", "")
                .replace("\r","")
                .replace("\n", "");
    }

    /**
     * Возвращает ревизию из файла
     *
     * @return  Возвращает исходный код накатного файла
     */
    private String getFileRevision(){
        String[] split = file.split("/");
        return split[0] + "/" + "web_deploy_revision";
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
        checkAttributes();
        attachedContents.add(new WebAttached(sourceCodeRevision, file));
        return attachedContents;
    }

    /**
     * Проверка необходимых атрибутов
     */
    @Override
    public void checkAttributes(){
        if(sourceCodeRevision == null){
            canDeploy = false;
        }
    }
}
