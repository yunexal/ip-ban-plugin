package com.yunexal.ipbanplugin.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.yunexal.ipbanplugin.database.DatabaseManager;
import com.yunexal.ipbanplugin.utils.MessageManager;
import net.kyori.adventure.text.Component;

public class ConnectionListener {
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;

    public ConnectionListener(DatabaseManager databaseManager, MessageManager messageManager) {
        this.databaseManager = databaseManager;
        this.messageManager = messageManager;
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        String ip = event.getConnection().getRemoteAddress().getAddress().getHostAddress();
        
        // We need to block here to ensure we check before allowing login.
        // Since PreLoginEvent can be async, we can join the future.
        // However, Velocity handles PreLoginEvent asynchronously by default if we don't block the main thread?
        // Actually, PreLoginEvent is fired on an event thread. We can block it safely if it's not the main thread.
        // But to be safe and correct with Velocity API, we should use the continuation if available or just join since it's async.
        // Velocity 3.x events are synchronous by default unless @Subscribe(async = true) is used?
        // No, PreLoginEvent is always fired asynchronously.
        
        try {
            boolean isBanned = databaseManager.isBanned(ip).join();
            if (isBanned) {
                Component banMessage = messageManager.getMessage("ban.message");
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(banMessage));
            }
        } catch (Exception e) {
            // If DB fails, we probably shouldn't block everyone, or maybe we should?
            // For security, maybe fail open or closed?
            // Let's log it and fail open for now to avoid locking everyone out on DB error.
            e.printStackTrace();
        }
    }
}
