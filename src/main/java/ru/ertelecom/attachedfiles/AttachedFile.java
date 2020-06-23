package ru.ertelecom.attachedfiles;

import java.util.List;

/**
 * Интерфейс для работы с накатными файлами всех типов
 * Содержит общие методы для всех накатных файлов
 */
public interface AttachedFile {
    //  Можно ли катать накатный файл
    boolean getCanDeploy();
    //  Получить инстансы накатных файлов
    List<Object> getAttachedContents();
    //  Проверить атрибуты накатного файла
    void checkAttributes();
    //  Получить исходный код накатного файла
    String getSourceCodeFromDeploy();
}
