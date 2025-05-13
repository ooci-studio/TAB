package me.neznamy.tab.platforms.paper;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaScoreboard;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaTabList;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation provider using direct Mojang-mapped NMS code (1.20.5+).
 */
@Getter
public class PaperImplementationProvider implements ImplementationProvider {

    @NotNull
    private final ComponentConverter componentConverter = new PaperComponentConverter();
    
    @Override
    @NotNull
    public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
        return new PaperPacketScoreboard(player);
    }

    @Override
    public void onPacketSend(@NonNull Object packet, @NonNull ViaScoreboard scoreboard) {
        PaperPacketScoreboard.onPacketSend(packet, scoreboard);
    }

    @Override
    public void onPacketSend(@NonNull Object packet, @NonNull ViaTabList tabList) {
        PaperPacketTabList.onPacketSend(packet, tabList);
    }

    @Override
    @NotNull
    public TabList newTabList(@NotNull BukkitTabPlayer player) {
        return new PaperPacketTabList(player);
    }

    @Override
    @NotNull
    public FunctionWithException<BukkitTabPlayer, Channel> getChannelFunction() {
        return player -> ((CraftPlayer)player.getPlayer()).getHandle().connection.connection.channel;
    }

    @Override
    public int getPing(@NotNull BukkitTabPlayer player) {
        return player.getPlayer().getPing();
    }
}