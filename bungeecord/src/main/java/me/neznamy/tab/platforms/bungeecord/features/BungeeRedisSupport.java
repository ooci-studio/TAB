package me.neznamy.tab.platforms.bungeecord.features;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import lombok.AllArgsConstructor;
import me.neznamy.tab.platforms.bungeecord.BungeeTAB;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * RedisBungee implementation for BungeeCord
 */
@AllArgsConstructor
public class BungeeRedisSupport extends ProxySupport implements Listener {

    /** Plugin reference for registering listener */
    @NotNull
    private final BungeeTAB plugin;

    /**
     * Listens to messages coming from other proxies.
     *
     * @param   e
     *          Message event
     */
    @EventHandler
    public void onMessage(@NotNull PubSubMessageEvent e) {
        if (!e.getChannel().equals(TabConstants.PROXY_CHANNEL_NAME)) return;
        processMessage(e.getMessage());
    }

    @Override
    public void register() {
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
        RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels(TabConstants.PROXY_CHANNEL_NAME);
    }

    @Override
    public void unregister() {
        ProxyServer.getInstance().getPluginManager().unregisterListener(this);
        RedisBungeeAPI.getRedisBungeeApi().unregisterPubSubChannels(TabConstants.PROXY_CHANNEL_NAME);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        try {
            RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(TabConstants.PROXY_CHANNEL_NAME, message);
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().redisBungeeMessageSendFail(e);
        }
    }
}