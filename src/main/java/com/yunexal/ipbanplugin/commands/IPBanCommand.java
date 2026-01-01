package com.yunexal.ipbanplugin.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.yunexal.ipbanplugin.database.DatabaseManager;
import com.yunexal.ipbanplugin.utils.MessageManager;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class IPBanCommand implements SimpleCommand {
    private final ProxyServer server;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    public IPBanCommand(ProxyServer server, DatabaseManager databaseManager, MessageManager messageManager) {
        this.server = server;
        this.databaseManager = databaseManager;
        this.messageManager = messageManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("ipbanplugin.ban")) {
            source.sendMessage(messageManager.getMessage("error.no_permission"));
            return;
        }

        if (args.length == 0) {
            // Show list
            databaseManager.getBannedIps().thenAccept(list -> {
                source.sendMessage(messageManager.getMessage("list.header"));
                if (list.isEmpty()) {
                    source.sendMessage(messageManager.getMessage("list.empty"));
                } else {
                    for (DatabaseManager.BannedIpInfo info : list) {
                        source.sendMessage(messageManager.getMessage("list.entry",
                                "ip", info.ip,
                                "banned_by", info.bannedBy));
                    }
                }
            });
            return;
        }

        String bannedBy = (source instanceof Player) ? ((Player) source).getUsername() : "Console";
        List<String> successfulBans = new ArrayList<>();
        List<String> alreadyBanned = new ArrayList<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String ip : args) {
            if (!isValidIp(ip)) {
                source.sendMessage(messageManager.getMessage("error.invalid_ip", "ip", ip));
                continue;
            }

            futures.add(databaseManager.banIp(ip, bannedBy).thenAccept(success -> {
                if (success) {
                    successfulBans.add(ip);
                    kickOnlinePlayers(ip);
                } else {
                    alreadyBanned.add(ip);
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            if (successfulBans.size() == 1) {
                source.sendMessage(messageManager.getMessage("ban.success", "ip", successfulBans.get(0)));
            } else if (successfulBans.size() > 1) {
                source.sendMessage(messageManager.getMessage("ban.success_multiple",
                        "count", String.valueOf(successfulBans.size()),
                        "ips", String.join(", ", successfulBans)));
            }

            if (alreadyBanned.size() == 1 && successfulBans.isEmpty()) {
                source.sendMessage(messageManager.getMessage("ban.already", "ip", alreadyBanned.get(0)));
            } else if (!alreadyBanned.isEmpty()) {
                source.sendMessage(messageManager.getMessage("ban.some_already",
                        "already_banned", String.join(", ", alreadyBanned)));
            }
        });
    }

    private boolean isValidIp(String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    private void kickOnlinePlayers(String ip) {
        Component kickMessage = messageManager.getMessage("ban.kick_online");
        server.getAllPlayers().stream()
                .filter(player -> player.getRemoteAddress().getAddress().getHostAddress().equals(ip))
                .forEach(player -> player.disconnect(kickMessage));
    }
    
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("ipbanplugin.ban");
    }
}
