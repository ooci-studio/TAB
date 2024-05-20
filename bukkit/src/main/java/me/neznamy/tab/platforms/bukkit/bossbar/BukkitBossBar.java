package me.neznamy.tab.platforms.bukkit.bossbar;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.jetbrains.annotations.NotNull;

/**
 * BossBar for 1.9+ servers where Bukkit API is used. If ViaVersion is used
 * to allow 1.8 players, it will handle the entity and teleporting by itself.
 */
@RequiredArgsConstructor
public class BukkitBossBar extends SafeBossBar<BossBar> {

    /** Style array because names do not match */
    private static final org.bukkit.boss.BarStyle[] styles = org.bukkit.boss.BarStyle.values();

    /** Player this handler belongs to */
    @NotNull
    private final BukkitTabPlayer player;

    @Override
    @NotNull
    public BossBar constructBossBar(@NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        BossBar bar = Bukkit.createBossBar(
                player.getPlatform().toBukkitFormat(TabComponent.optimized(title), player.getVersion().supportsRGB()),
                org.bukkit.boss.BarColor.valueOf(color.name()),
                styles[style.ordinal()]
        );
        bar.setProgress(progress);
        return bar;
    }

    @Override
    public void create(SafeBossBar<BossBar>.@NotNull BossBarInfo bar) {
        bar.getBossBar().addPlayer(player.getPlayer());
    }

    @Override
    public void updateTitle(SafeBossBar<BossBar>.@NotNull BossBarInfo bar) {
        bar.getBossBar().setTitle(player.getPlatform().toBukkitFormat(TabComponent.optimized(bar.getTitle()), player.getVersion().supportsRGB()));
    }

    @Override
    public void updateProgress(SafeBossBar<BossBar>.@NotNull BossBarInfo bar) {
        bar.getBossBar().setProgress(bar.getProgress());
    }

    @Override
    public void updateStyle(SafeBossBar<BossBar>.@NotNull BossBarInfo bar) {
        bar.getBossBar().setStyle(styles[bar.getStyle().ordinal()]);
    }

    @Override
    public void updateColor(SafeBossBar<BossBar>.@NotNull BossBarInfo bar) {
        bar.getBossBar().setColor(org.bukkit.boss.BarColor.valueOf(bar.getColor().name()));
    }

    @Override
    public void remove(SafeBossBar<BossBar>.@NotNull BossBarInfo bar) {
        bar.getBossBar().removePlayer(player.getPlayer());
    }
}
