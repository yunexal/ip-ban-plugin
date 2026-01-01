package com.yunexal.ipbanplugin.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.yunexal.ipbanplugin.utils.ConfigManager;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    private final Logger logger;
    private final Path dataDirectory;
    private final Path dbFile;
    private final Gson gson;
    private final Map<String, BannedIpInfo> bans = new ConcurrentHashMap<>();

    @Inject
    public DatabaseManager(ConfigManager configManager, Logger logger, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.dbFile = dataDirectory.resolve("banned_ips.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void init() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            if (Files.exists(dbFile)) {
                loadBans();
            } else {
                saveBans();
            }
            
            logger.info("Database initialized (JSON storage). Loaded " + bans.size() + " bans.");

        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
        }
    }

    private void loadBans() {
        try (BufferedReader reader = Files.newBufferedReader(dbFile, java.nio.charset.StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<BannedIpInfo>>(){}.getType();
            List<BannedIpInfo> list = gson.fromJson(reader, listType);
            if (list != null) {
                for (BannedIpInfo info : list) {
                    bans.put(info.ip, info);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load bans from file", e);
        }
    }

    private synchronized void saveBans() {
        try (BufferedWriter writer = Files.newBufferedWriter(dbFile, java.nio.charset.StandardCharsets.UTF_8)) {
            gson.toJson(new ArrayList<>(bans.values()), writer);
        } catch (IOException e) {
            logger.error("Failed to save bans to file", e);
        }
    }

    public void close() {
        saveBans();
    }

    public CompletableFuture<Boolean> banIp(String ip, String bannedBy) {
        return CompletableFuture.supplyAsync(() -> {
            if (bans.containsKey(ip)) {
                return false;
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            BannedIpInfo info = new BannedIpInfo(ip, bannedBy, timestamp);
            bans.put(ip, info);
            saveBans();
            return true;
        });
    }

    public CompletableFuture<Boolean> unbanIp(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            if (!bans.containsKey(ip)) {
                return false;
            }
            bans.remove(ip);
            saveBans();
            return true;
        });
    }

    public CompletableFuture<Boolean> isBanned(String ip) {
        return CompletableFuture.completedFuture(bans.containsKey(ip));
    }

    public CompletableFuture<List<BannedIpInfo>> getBannedIps() {
        return CompletableFuture.completedFuture(new ArrayList<>(bans.values()));
    }

    public static class BannedIpInfo {
        public final String ip;
        public final String bannedBy;
        public final String bannedAt;

        public BannedIpInfo(String ip, String bannedBy, String bannedAt) {
            this.ip = ip;
            this.bannedBy = bannedBy;
            this.bannedAt = bannedAt;
        }
    }
}
