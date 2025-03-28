package me.neznamy.tab.platforms.bukkit.tablist;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * TabList handler for 1.8 - 1.19.2 servers using packets.
 */
@Setter
@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketTabList18 extends TrackedTabList<BukkitTabPlayer> {

    @Nullable
    public static SkinData skinData;

    protected static Class<?> PlayerInfoClass;
    protected static Constructor<?> newPlayerInfo;
    protected static Field ACTION;
    protected static Field PLAYERS;
    protected static Class<Enum> ActionClass;

    protected static Constructor<?> newPlayerInfoData;
    protected static Field PlayerInfoData_Profile;
    protected static Field PlayerInfoData_Latency;
    protected static Field PlayerInfoData_DisplayName;

    protected static Object[] gameModes;

    protected static PacketSender packetSender;

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this tablist will belong to.
     */
    public PacketTabList18(@NonNull BukkitTabPlayer player) {
        super(player);
    }

    /**
     * Attempts to load all required NMS classes, fields and methods.
     * If anything fails, throws an exception.
     *
     * @throws  ReflectiveOperationException
     *          If something goes wrong
     */
    public static void load() throws ReflectiveOperationException {
        Class<Enum> EnumGamemodeClass = (Class<Enum>) BukkitReflection.getClass("world.level.GameType",
                "world.level.EnumGamemode", "EnumGamemode", "WorldSettings$EnumGamemode");
        ActionClass = (Class<Enum>) BukkitReflection.getClass(
                "network.protocol.game.ClientboundPlayerInfoPacket$Action", // Mojang 1.17 - 1.19.2
                "network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction", // Bukkit 1.17 - 1.19.2
                "PacketPlayOutPlayerInfo$EnumPlayerInfoAction", // Bukkit 1.8.1 - 1.16.5
                "EnumPlayerInfoAction" // Bukkit 1.8.0
        );
        PlayerInfoClass = BukkitReflection.getClass("network.protocol.game.ClientboundPlayerInfoUpdatePacket",
                "network.protocol.game.ClientboundPlayerInfoPacket",
                "network.protocol.game.PacketPlayOutPlayerInfo", "PacketPlayOutPlayerInfo");
        Class<?> playerInfoDataClass = BukkitReflection.getClass(
                "network.protocol.game.ClientboundPlayerInfoPacket$PlayerUpdate", // Mojang 1.17 - 1.19.2
                "network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData", // Bukkit 1.17 - 1.19.2
                "PacketPlayOutPlayerInfo$PlayerInfoData", // Bukkit 1.8.1 - 1.16.5
                "PlayerInfoData" // Bukkit 1.8.0
        );

        Class<?> classType = BukkitReflection.getMinorVersion() >= 17 ? Collection.class : Iterable.class;
        newPlayerInfo = PlayerInfoClass.getConstructor(ActionClass, classType);
        ACTION = ReflectionUtils.getOnlyField(PlayerInfoClass, ActionClass);

        loadSharedContent(playerInfoDataClass, EnumGamemodeClass);

        newPlayerInfoData = playerInfoDataClass.getConstructors()[0]; // #1105, a specific 1.8.8 fork has 2 constructors
    }

    protected static void loadSharedContent(Class<?> infoData, Class<Enum> gameMode) throws ReflectiveOperationException {
        Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
        PLAYERS = ReflectionUtils.getOnlyField(PlayerInfoClass, List.class);
        PlayerInfoData_Profile = ReflectionUtils.getOnlyField(infoData, GameProfile.class);
        PlayerInfoData_Latency = ReflectionUtils.getFields(infoData, int.class).get(0);
        PlayerInfoData_DisplayName = ReflectionUtils.getOnlyField(infoData, IChatBaseComponent);
        gameModes = new Object[] {
                Enum.valueOf(gameMode, "SURVIVAL"),
                Enum.valueOf(gameMode, "CREATIVE"),
                Enum.valueOf(gameMode, "ADVENTURE"),
                Enum.valueOf(gameMode, "SPECTATOR")
        };
        packetSender = new PacketSender();
        try {
            skinData = new SkinData();
        } catch (Exception e) {
            BukkitUtils.compatibilityError(e, "getting player's game profile", null,
                    "Player skins not working in layout feature");
        }
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        packetSender.sendPacket(player,
                createPacket(Action.REMOVE_PLAYER, entry, "", null, false, 0, 0, null, 0, false));
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        packetSender.sendPacket(player,
                createPacket(Action.UPDATE_DISPLAY_NAME, entry, "", null, false, 0, 0, displayName, 0, false));
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        packetSender.sendPacket(player,
                createPacket(Action.UPDATE_LATENCY, entry, "", null, false, latency, 0, null, 0, false));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        packetSender.sendPacket(player,
                createPacket(Action.UPDATE_GAME_MODE, entry, "", null, false, 0, gameMode, null, 0, false));
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // Added in 1.19.3
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        // Added in 1.21.2
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        // Added in 1.21.4
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        packetSender.sendPacket(player,
                createPacket(Action.ADD_PLAYER, entry.getUniqueId(), entry.getName(), entry.getSkin(), entry.isListed(),
                        entry.getLatency(), entry.getGameMode(), entry.getDisplayName(), entry.getListOrder(), entry.isShowHat()));
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.getPlatform().getHeaderFooter().set(player, header, footer);
    }

    /**
     * Creates packet from given parameters.
     *
     * @param   action
     *          Packet action
     * @param   id
     *          Entry UUID
     * @param   name
     *          Entry name
     * @param   skin
     *          Entry skin
     * @param   listed
     *          Whether entry should be listed or not
     * @param   latency
     *          Entry latency
     * @param   gameMode
     *          Entry game mode
     * @param   displayName
     *          Entry display name
     * @param   listOrder
     *          Entry list order
     * @param   showHat
     *          Show hat flag
     * @return  Packet from given parameters
     */
    @SneakyThrows
    @NotNull
    public Object createPacket(@NonNull Action action, @NonNull UUID id, @NonNull String name, @Nullable Skin skin,
                               boolean listed, int latency, int gameMode, @Nullable TabComponent displayName, int listOrder, boolean showHat) {
        Object packet = newPlayerInfo.newInstance(Enum.valueOf(ActionClass, action.name()), Collections.emptyList());
        List<Object> parameters = new ArrayList<>();
        if (newPlayerInfoData.getParameterTypes()[0] == PlayerInfoClass) {
            parameters.add(packet);
        }
        parameters.add(createProfile(id, name, skin));
        parameters.add(latency);
        parameters.add(gameModes[gameMode]);
        parameters.add(displayName == null ? null : displayName.convert());
        if (BukkitReflection.getMinorVersion() >= 19) parameters.add(null);
        PLAYERS.set(packet, Collections.singletonList(newPlayerInfoData.newInstance(parameters.toArray())));
        return packet;
    }

    /**
     * Creates GameProfile from given parameters.
     *
     * @param   id
     *          Profile ID
     * @param   name
     *          Profile name
     * @param   skin
     *          Player skin
     * @return  GameProfile from given parameters
     */
    @NotNull
    public GameProfile createProfile(@NonNull UUID id, @NonNull String name, @Nullable Skin skin) {
        GameProfile profile = new GameProfile(id, name);
        if (skin != null) {
            profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                    new Property(TabList.TEXTURES_PROPERTY, skin.getValue(), skin.getSignature()));
        }
        return profile;
    }

    @Override
    @SneakyThrows
    public void onPacketSend(@NonNull Object packet) {
        if (!(PlayerInfoClass.isInstance(packet))) return;
        String action = ACTION.get(packet).toString();
        for (Object nmsData : (List<?>) PLAYERS.get(packet)) {
            GameProfile profile = (GameProfile) PlayerInfoData_Profile.get(nmsData);
            UUID id = profile.getId();
            if (action.equals(Action.UPDATE_DISPLAY_NAME.name()) || action.equals(Action.ADD_PLAYER.name())) {
                TabComponent expectedName = getExpectedDisplayNames().get(id);
                if (expectedName != null) PlayerInfoData_DisplayName.set(nmsData, expectedName.convert());
            }
            if (action.equals(Action.UPDATE_LATENCY.name()) || action.equals(Action.ADD_PLAYER.name())) {
                int oldLatency = PlayerInfoData_Latency.getInt(nmsData);
                int newLatency = TAB.getInstance().getFeatureManager().onLatencyChange(player, id, oldLatency);
                if (oldLatency != newLatency) {
                    PlayerInfoData_Latency.set(nmsData, newLatency);
                }
            }
            if (action.equals(Action.ADD_PLAYER.name())) {
                TAB.getInstance().getFeatureManager().onEntryAdd(player, id, profile.getName());
            }
        }
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return true; // TODO?
    }

    @Nullable
    @Override
    public Skin getSkin() {
        if (skinData == null) return null;
        return skinData.getSkin(player);
    }
}
