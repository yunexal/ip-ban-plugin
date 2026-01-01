package com.yunexal.ipbanplugin.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.yunexal.ipbanplugin.database.DatabaseManager;
import com.yunexal.ipbanplugin.utils.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class UnbanIPCommand implements SimpleCommand {
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    public UnbanIPCommand(DatabaseManager databaseManager, MessageManager messageManager) {
        this.databaseManager = databaseManager;
        this.messageManager = messageManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("ipbanplugin.unban")) {
            source.sendMessage(messageManager.getMessage("error.no_permission"));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(messageManager.getMessage("usage.unbanip"));
            return;
        }

        List<String> successfulUnbans = new ArrayList<>();
        List<String> notBanned = new ArrayList<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String ip : args) {
            if (!isValidIp(ip)) {
                source.sendMessage(messageManager.getMessage("error.invalid_ip", "ip", ip));
                continue;
            }

            futures.add(databaseManager.unbanIp(ip).thenAccept(success -> {
                if (success) {
                    successfulUnbans.add(ip);
                } else {
                    notBanned.add(ip);
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            if (successfulUnbans.size() == 1) {
                source.sendMessage(messageManager.getMessage("unban.success", "ip", successfulUnbans.get(0)));
            } else if (successfulUnbans.size() > 1) {
                source.sendMessage(messageManager.getMessage("unban.success_multiple",
                        "count", String.valueOf(successfulUnbans.size()),
                        "ips", String.join(", ", successfulUnbans)));
            }

            if (notBanned.size() == 1 && successfulUnbans.isEmpty()) {
                source.sendMessage(messageManager.getMessage("unban.not_banned", "ip", notBanned.get(0)));
            } else if (!notBanned.isEmpty()) {
                source.sendMessage(messageManager.getMessage("unban.some_not_banned",
                        "not_banned", String.join(", ", notBanned)));
            }
        });
    }

    private boolean isValidIp(String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }
    
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("ipbanplugin.unban");
    }
}
