package com.yunexal.ipbanplugin.utils;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class MessageManager {
    private final Path dataDirectory;
    private final Logger logger;
    private Properties messages;

    @Inject
    public MessageManager(@DataDirectory Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.messages = new Properties();
    }

    public void loadMessages() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            File messagesFile = dataDirectory.resolve("messages.properties").toFile();
            if (!messagesFile.exists()) {
                try (InputStream in = getClass().getResourceAsStream("/messages.properties")) {
                    if (in != null) {
                        Files.copy(in, messagesFile.toPath());
                    } else {
                        messagesFile.createNewFile();
                    }
                }
            }

            try (InputStream in = new FileInputStream(messagesFile);
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                messages.load(reader);
            }
        } catch (IOException e) {
            logger.error("Failed to load messages.properties", e);
        }
    }

    public String getRawMessage(String key) {
        return messages.getProperty(key, "Missing message: " + key);
    }

    public Component getMessage(String key) {
        String message = getRawMessage(key);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public Component getMessage(String key, String... placeholders) {
        String message = getRawMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
            }
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }
}
