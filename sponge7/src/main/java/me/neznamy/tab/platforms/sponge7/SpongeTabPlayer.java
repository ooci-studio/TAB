package me.neznamy.tab.platforms.sponge7;

import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.Text;

import java.util.Collection;

/**
 * TabPlayer implementation for Sponge 7.
 */
public class SpongeTabPlayer extends BackendTabPlayer {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   platform
     *          Server platform
     * @param   player
     *          Platform's player object
     */
    public SpongeTabPlayer(@NotNull SpongePlatform platform, @NotNull Player player) {
        super(platform, player, player.getUniqueId(), player.getName(), player.getWorld().getName(), platform.getServerVersion().getNetworkId());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getConnection().getLatency();
    }

    @Override
    public void sendMessage(@NotNull TabComponent message) {
        getPlayer().sendMessage(Text.of(message.toLegacyText()));
    }

    @Override
    public boolean hasInvisibilityPotion() {
        PotionEffectData potionEffects = getPlayer().get(PotionEffectData.class).orElse(null);
        if (potionEffects == null) return false;
        return potionEffects.asList().stream().anyMatch(effect -> effect.getType().equals(PotionEffectTypes.INVISIBILITY));
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    @Nullable
    public TabList.Skin getSkin() {
        Collection<ProfileProperty> properties = getPlayer().getProfile().getPropertyMap().get(TabList.TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null; // Offline mode
        ProfileProperty property = properties.iterator().next();
        return new TabList.Skin(property.getValue(), property.getSignature().orElse(null));
    }

    @Override
    @NotNull
    public Player getPlayer() {
        return (Player) player;
    }

    @Override
    public SpongePlatform getPlatform() {
        return (SpongePlatform) platform;
    }

    @Override
    public boolean isVanished0() {
        return getPlayer().get(Keys.VANISH).orElse(false);
    }

    @Override
    public int getGamemode() {
        GameMode gameMode = getPlayer().getGameModeData().type().get();
        if (gameMode.equals(GameModes.CREATIVE)) return 1;
        if (gameMode.equals(GameModes.ADVENTURE)) return 2;
        if (gameMode.equals(GameModes.SPECTATOR)) return 3;
        return 0;
    }

    @Override
    public double getHealth() {
        return getPlayer().health().get();
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return getPlayer().getDisplayNameData().displayName().get().toPlain();
    }
}
