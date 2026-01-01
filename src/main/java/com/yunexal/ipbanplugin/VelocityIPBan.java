package com.yunexal.ipbanplugin;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.yunexal.ipbanplugin.commands.IPBanCommand;
import com.yunexal.ipbanplugin.commands.UnbanIPCommand;
import com.yunexal.ipbanplugin.database.DatabaseManager;
import com.yunexal.ipbanplugin.listeners.ConnectionListener;
import com.yunexal.ipbanplugin.utils.ConfigManager;
import com.yunexal.ipbanplugin.utils.MessageManager;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "ipbanplugin",
        name = "IPBanPlugin",
        version = "1.0.1",
        description = "A simple IP ban plugin for Velocity",
        authors = {"Yunexal"}
)
public class VelocityIPBan {

    private final ProxyServer server;
    private final Logger logger;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final DatabaseManager databaseManager;

    @Inject
    public VelocityIPBan(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.configManager = new ConfigManager(dataDirectory, logger);
        this.messageManager = new MessageManager(dataDirectory, logger);
        this.databaseManager = new DatabaseManager(configManager, logger, dataDirectory);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("VelocityIPBan is starting...");

        // Load config and messages
        configManager.loadConfig();
        messageManager.loadMessages();

        // Initialize database
        databaseManager.init();

        // Register commands
        CommandManager commandManager = server.getCommandManager();
        
        CommandMeta ipBanMeta = commandManager.metaBuilder("ipban")
                .plugin(this)
                .build();
        commandManager.register(ipBanMeta, new IPBanCommand(server, databaseManager, messageManager));

        CommandMeta unbanIpMeta = commandManager.metaBuilder("unbanip")
                .plugin(this)
                .build();
        commandManager.register(unbanIpMeta, new UnbanIPCommand(databaseManager, messageManager));

        // Register listeners
        server.getEventManager().register(this, new ConnectionListener(databaseManager, messageManager));

        logger.info("VelocityIPBan has been enabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        databaseManager.close();
        logger.info("VelocityIPBan has been disabled!");
    }
}
