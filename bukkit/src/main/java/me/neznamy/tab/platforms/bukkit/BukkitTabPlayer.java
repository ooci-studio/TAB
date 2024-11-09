package me.neznamy.tab.platforms.bukkit;

import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.hook.LibsDisguisesHook;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PingRetriever;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.tablist.TabListBase;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TabPlayer implementation for Bukkit platform
 */
@SuppressWarnings("deprecation")
public class BukkitTabPlayer extends BackendTabPlayer {

    /** NMS handle of this player */
    @NotNull
    @Getter
    private final Object handle;

    /** Player's connection for sending packets */
    @Nullable
    public Object connection;

    /**
     * Constructs new instance with given bukkit player
     *
     * @param   platform
     *          Server platform
     * @param   p
     *          bukkit player
     */
    @SneakyThrows
    public BukkitTabPlayer(@NotNull BukkitPlatform platform, @NotNull Player p) {
        super(platform, p, p.getUniqueId(), p.getName(), p.getWorld().getName(), platform.getServerVersion().getNetworkId());
        handle = BukkitReflection.CraftPlayer_getHandle.invoke(p);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return PingRetriever.getPing(this);
    }

    @Override
    public void sendMessage(@NotNull TabComponent message) {
        getPlayer().sendMessage(getPlatform().toBukkitFormat(message, getVersion().supportsRGB()));
    }

    @Override
    public boolean hasInvisibilityPotion() {
        return getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Override
    public boolean isDisguised() {
        return LibsDisguisesHook.isDisguised(this);
    }

    @Override
    @Nullable
    public TabList.Skin getSkin() {
        return ((TabListBase<?>)getTabList()).getSkin();
    }

    @Override
    @NotNull
    public Player getPlayer() {
        return (Player) player;
    }

    @Override
    public BukkitPlatform getPlatform() {
        return (BukkitPlatform) platform;
    }

    @Override
    public boolean isVanished0() {
        for (MetadataValue v : getPlayer().getMetadata("vanished")) {
            if (v.asBoolean()) return true;
        }
        return false;
    }

    @Override
    public int getGamemode() {
        return getPlayer().getGameMode().getValue();
    }

    @Override
    public double getHealth() {
        return getPlayer().getHealth();
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return getPlayer().getDisplayName();
    }
}
