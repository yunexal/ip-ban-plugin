package com.yunexal.ipbanplugin.utils;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ConfigManager {
    private final Path dataDirectory;
    private final Logger logger;
    private Map<String, Object> config;

    @Inject
    public ConfigManager(@DataDirectory Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }
    
    public void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            File configFile = dataDirectory.resolve("config.yml").toFile();
            if (!configFile.exists()) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile.toPath());
                    } else {
                        configFile.createNewFile();
                    }
                }
            }

            try (InputStream in = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                config = yaml.load(in);
            }
        } catch (IOException e) {
            logger.error("Failed to load config.yml", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path) {
        if (config == null) return null;
        String[] keys = path.split("\\.");
        Object current = config;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                return null;
            }
        }
        return (T) current;
    }
    
    public String getDatabaseFile() {
        String file = get("database.sqlite.file");
        return file != null ? file : "banned_ips.db";
    }
}
