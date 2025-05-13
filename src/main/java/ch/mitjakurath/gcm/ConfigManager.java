package ch.mitjakurath.gcm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {

    private static final String CONFIG_DIR_NAME = ".git-config-manager";
    private static final String CONFIG_FILE_NAME = "config.json";

    private final Path configFilePath;
    private final ObjectMapper objectMapper;

    public ConfigManager() {
        Path homeDir = Paths.get(System.getProperty("user.home"));
        Path configDir = homeDir.resolve(CONFIG_DIR_NAME);
        this.configFilePath = configDir.resolve(CONFIG_FILE_NAME);

        this.objectMapper = new ObjectMapper();

        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Path getConfigDir() {
        return configFilePath.getParent();
    }

    public AppConfig loadConfig() throws IOException {
        if (!Files.exists(configFilePath)) {
            Files.createDirectories(configFilePath.getParent());
            AppConfig defaultConfig = new AppConfig();
            saveConfig(defaultConfig);
            return defaultConfig;
        }
        return objectMapper.readValue(configFilePath.toFile(), AppConfig.class);
    }

    public void saveConfig(AppConfig config) throws IOException {
        Files.createDirectories(configFilePath.getParent());
        objectMapper.writeValue(configFilePath.toFile(), config);
    }
}