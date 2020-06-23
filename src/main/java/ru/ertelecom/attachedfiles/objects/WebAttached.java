package ru.ertelecom.attachedfiles.objects;

import lombok.Getter;
import ru.ertelecom.config.MainConfig;
import ru.ertelecom.git.GitWork;

/**
 * Класс для работы с вебом в накатных файлах
 */
public class WebAttached {
    private @Getter String revision;
    private @Getter String pathToFile;
    private @Getter String sourceCode;

    /**
     * Конструктор класса Web
     *
     * @param revision      Ревизия из файла web_deploy_revision
     * @param pathToFile    Путь к web файлу в репозитории Deploy
     */
    public WebAttached(String revision, String pathToFile) {
        this.revision = revision;
        this.pathToFile = pathToFile;
        sourceCode = getSourceCodeFromDeploy();
    }

    /**
     * Возвращает исходный код файла из репозитория Deploy
     *
     * @return  Возвращает исходный код файла
     */
    private String getSourceCodeFromDeploy() {
        return new GitWork(MainConfig.CONFIG.getDeployRepoPath()).gitGetSourceCode("master",  pathToFile);
    }
}
