package ru.ertelecom.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import ru.ertelecom.logger.Logger;
import ru.ertelecom.logger.LoggerEventType;
import ru.ertelecom.logger.LoggerManager;

import java.io.File;
import java.io.IOException;

/**
 * Класс для работы с GIT
 */
public class GitWork {

    private Logger logger = LoggerManager.getLogger(GitWork.class);
    private String workingDirectory;
    private Repository repository;
    private Git git;

    /**
     * Конструктор класса для работы с GIT
     *
     * @param workingDirectory  путь до репозитория
     */
    public GitWork(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        setRepository();
        setGit();
    }

    /**
     * Метод для обновления репозитория
     */
    public void gitUpdateRepository(){
        try {
            PullResult result = git.pull().call();
            logger.log(LoggerEventType.GIT_INFO,
                    String.format("Обновление репозитория %s: %s", repository.getDirectory(), result.getMergeResult()));

        } catch (Exception e) {
            logger.log(LoggerEventType.GIT_ERROR,
                    String.format("Ошибка при обновлении репозитория.\nТекст ошибки: %s", e.getMessage()));
        }
    }

    /**
     * Метод для переключения на нужный тэг
     *
     * @param tag  тэг на который необходимо переключиться
     */
    public void gitCheckoutTag(String tag) {
        try {
            git.checkout()
                    .setName("refs/tags/" + tag)
                    .call();
            logger.log(LoggerEventType.GIT_INFO,
                    String.format("На тэг %s переключился. Репозиторий: %s", tag, repository.getDirectory()));
        } catch (Exception e) {
            logger.log(LoggerEventType.GIT_ERROR,
                    String.format("Ошибка при переключении на тэг %s. Репозиторий: %s\nТекст ошибки: %s", tag,
                            repository.getDirectory(), e.getMessage()));
        }

    }
    /**
     * Метод для переключения на нужную ветку
     *
     * @param branch    ветка на которую необходимо переключиться
     */
    public void gitCheckoutBranch(String branch){
        try {
            git.checkout()
                    .setName(branch)
                    .call();
            logger.log(LoggerEventType.GIT_INFO,
                    String.format("На ветку %s переключился. Репозиторий: %s", branch, repository.getDirectory()));
        } catch (Exception e) {
            logger.log(LoggerEventType.GIT_ERROR,
                    String.format("Ошибка при переключении на ветку %s. Репозиторий: %s\nТекст ошибки: %s", branch,
                            repository.getDirectory(), e.getMessage()));
        }
    }

    /**
     * Метод для получения исходного кода
     *
     * @param tag    ревизия по которой необходимо достать исходный код
     * @param file   путь до файла из которого необходимо достать исходный код
     */
    public String gitGetSourceCode(String tag, String file){
        try (ObjectReader reader = repository.newObjectReader()) {
            ObjectId id = repository.findRef(tag).getObjectId();
            RevWalk walk = new RevWalk(reader);
            RevCommit commit = walk.parseCommit(id);
            RevTree tree = commit.getTree();
            TreeWalk treewalk = TreeWalk.forPath(reader, file, tree);
            if (treewalk != null) {
                byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
                logger.log(LoggerEventType.GIT_INFO,
                        String.format("Файл %s по тэгу %s выгружен. Репозиторий: %s", file, tag,
                                repository.getDirectory()));
                return new String(data, "Windows-1251");
            } else {
                logger.log(LoggerEventType.GIT_ERROR,
                        String.format("Файл %s отсутствует в репозитории %s: ", file, repository.getDirectory()));
                return null;
            }
        } catch (IOException e) {
            logger.log(LoggerEventType.GIT_ERROR,
                    String.format("Ошибка чтения файла %s. Репозиторий %s\nТекст ошибки: %s", file,
                            repository.getDirectory(), e.getMessage()));
            return null;
        } catch (NullPointerException e) {
            logger.log(LoggerEventType.GIT_ERROR,
                    String.format("Ошибка при выгрузке файла %s из репозитория %s, проверьте тэг %s\nТекст ошибки: %s",
                            file, repository.getDirectory(), tag, e.getMessage()));
            return null;
        }
    }
    /**
     * Setter для объекта Repository
     */
    private void setRepository(){
        try {
            this.repository = new FileRepositoryBuilder()
                    .setGitDir(new File(workingDirectory))
                    .build();
        } catch (Exception e) {
            logger.log(LoggerEventType.GIT_ERROR,
                    String.format("Ошибка чтения пути. Проверьте путь до репозитория %s.\nТекст ошибки: %s", workingDirectory, e));
        }
    }
    /**
     * Setter для объекта Git
     */
    private void setGit() {
        try {
            this.git = new Git(repository);
        } catch (Exception e) {
            logger.log(LoggerEventType.GIT_ERROR,
                    String.format("Ошибка чтения пути. Проверьте путь до репозитория %s.\nТекст ошибки: %s",workingDirectory, e));
        }
    }
}
