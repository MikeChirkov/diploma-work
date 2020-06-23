package ru.ertelecom.config;

import java.io.File;

public class ConfigPath {
    private static final String configs_path = System.getProperty("user.dir") + File.separator + "config" + File.separator;
    public static final String jira_config_path = configs_path + "jira_config.json";
    public static final String main_config_path = configs_path + "main_config.json";
    public static final String heavy_packages_path = configs_path + "heavy_packages.json";
    public static final String server_config_path = configs_path + "server_config.json";
    public static final String п = configs_path + "logger_config.json";
}
