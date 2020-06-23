package ru.ertelecom.attachedfiles;

import ru.ertelecom.attachedfiles.errors.FileExtensionNotSupportedError;

/**
 * Фабрика для получения конкретной реализации DeployFile, в зависимости от переданного в аргументы файла.
 */
public class AttachedFileFactory {

    public static AttachedFile getAttachedFile(String file) {

        if (file.endsWith(".sql")) {
            return new SqlAttachedFile(file);
        } else if (file.endsWith(".web")) {
            return new WebAttachedFile(file);
        } else if (file.endsWith("_revision")){
            return null;
        }
        throw new FileExtensionNotSupportedError();
    }
}

