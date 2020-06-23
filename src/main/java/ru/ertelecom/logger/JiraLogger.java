package ru.ertelecom.logger;

public class JiraLogger {

    private StringBuilder stringBuilder = new StringBuilder("");

    public void append(String message){
        stringBuilder.append(message);
    }

    public void delete(Integer firstIndex, Integer lastIndex){
        stringBuilder.delete(firstIndex, lastIndex);
    }

    public String getFullMessage() {
        return stringBuilder.toString();
    }

    public void clearMessage(){
        stringBuilder.delete(0, stringBuilder.length());
    }
}