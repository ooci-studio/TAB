package me.neznamy.tab.platforms.bukkit.v1_12_R1;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

/**
 * Scoreboard implementation using direct NMS code.
 */
public class NMSPacketScoreboard extends SafeScoreboard<BukkitTabPlayer> {

    private static final ScoreboardTeamBase.EnumNameTagVisibility[] visibilities = ScoreboardTeamBase.EnumNameTagVisibility.values();
    private static final ScoreboardTeamBase.EnumTeamPush[] collisions = ScoreboardTeamBase.EnumTeamPush.values();
    private static final Scoreboard dummyScoreboard = new Scoreboard();

    private static final Field TeamPacket_NAME = ReflectionUtils.getFields(PacketPlayOutScoreboardTeam.class, String.class).get(0);
    private static final Field TeamPacket_ACTION = ReflectionUtils.getInstanceFields(PacketPlayOutScoreboardTeam.class, int.class).get(1);
    private static final Field TeamPacket_PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardTeam.class, Collection.class);

    private static final Field Objective_OBJECTIVE_NAME = ReflectionUtils.getFields(PacketPlayOutScoreboardObjective.class, String.class).get(0);
    private static final Field Objective_METHOD = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardObjective.class, int.class);

    private static final Field DisplayObjective_OBJECTIVE_NAME = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardDisplayObjective.class, String.class);
    private static final Field DisplayObjective_POSITION = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardDisplayObjective.class, int.class);

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static void onPacketSend(@NonNull Object packet, @NonNull SafeScoreboard<BukkitTabPlayer> scoreboard) {
        if (scoreboard.isAntiOverrideScoreboard()) {
            if (packet instanceof PacketPlayOutScoreboardDisplayObjective) {
                TAB.getInstance().getFeatureManager().onDisplayObjective(scoreboard.getPlayer(), DisplayObjective_POSITION.getInt(packet),
                        (String) DisplayObjective_OBJECTIVE_NAME.get(packet));
            }
            if (packet instanceof PacketPlayOutScoreboardObjective) {
                TAB.getInstance().getFeatureManager().onObjective(scoreboard.getPlayer(),
                        Objective_METHOD.getInt(packet), (String) Objective_OBJECTIVE_NAME.get(packet));
            }
        }
        if (scoreboard.isAntiOverrideTeams() && packet instanceof PacketPlayOutScoreboardTeam) {
            int action = TeamPacket_ACTION.getInt(packet);
            if (action == TeamAction.UPDATE) return;
            Collection<String> players = (Collection<String>) TeamPacket_PLAYERS.get(packet);
            if (players == null) players = Collections.emptyList();
            TeamPacket_PLAYERS.set(packet, scoreboard.onTeamPacket(action, (String) TeamPacket_NAME.get(packet), players));
        }
    }

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public NMSPacketScoreboard(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        ScoreboardObjective obj = new ScoreboardObjective(dummyScoreboard, objective.getName(), IScoreboardCriteria.b);
        obj.setDisplayName(cutTo(objective.getTitle().toLegacyText(), Limitations.SCOREBOARD_TITLE_PRE_1_13));
        obj.a(IScoreboardCriteria.EnumScoreboardHealthDisplay.valueOf(objective.getHealthDisplay().name()));
        objective.setPlatformObjective(obj);
        sendPacket(new PacketPlayOutScoreboardObjective(obj, ObjectiveAction.REGISTER));
    }

    @Override
    public void setDisplaySlot(@NonNull Objective objective) {
        sendPacket(new PacketPlayOutScoreboardDisplayObjective(objective.getDisplaySlot().ordinal(), (ScoreboardObjective) objective.getPlatformObjective()));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        sendPacket(new PacketPlayOutScoreboardObjective((ScoreboardObjective) objective.getPlatformObjective(), ObjectiveAction.UNREGISTER));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        ScoreboardObjective obj = (ScoreboardObjective) objective.getPlatformObjective();
        obj.setDisplayName(cutTo(objective.getTitle().toLegacyText(), Limitations.SCOREBOARD_TITLE_PRE_1_13));
        obj.a(IScoreboardCriteria.EnumScoreboardHealthDisplay.valueOf(objective.getHealthDisplay().name()));
        sendPacket(new PacketPlayOutScoreboardObjective(obj, ObjectiveAction.UPDATE));
    }

    @Override
    public void setScore(@NonNull Score score) {
        ScoreboardScore s = new ScoreboardScore(dummyScoreboard, (ScoreboardObjective) score.getObjective().getPlatformObjective(), score.getHolder());
        s.setScore(score.getValue());
        sendPacket(new PacketPlayOutScoreboardScore(s));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        sendPacket(new PacketPlayOutScoreboardScore(score.getHolder()));
    }

    @Override
    @NotNull
    public Object createTeam(@NonNull String name) {
        return new ScoreboardTeam(dummyScoreboard, name);
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        updateTeamProperties(team);
        ScoreboardTeam t = (ScoreboardTeam) team.getPlatformTeam();
        t.getPlayerNameSet().addAll(team.getPlayers());
        sendPacket(new PacketPlayOutScoreboardTeam(t, TeamAction.CREATE));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        sendPacket(new PacketPlayOutScoreboardTeam((ScoreboardTeam) team.getPlatformTeam(), TeamAction.REMOVE));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        updateTeamProperties(team);
        sendPacket(new PacketPlayOutScoreboardTeam((ScoreboardTeam) team.getPlatformTeam(), TeamAction.UPDATE));
    }

    private void updateTeamProperties(@NonNull Team team) {
        ScoreboardTeam t = (ScoreboardTeam) team.getPlatformTeam();
        t.setAllowFriendlyFire((team.getOptions() & 0x01) != 0);
        t.setCanSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0);
        t.setNameTagVisibility(visibilities[team.getVisibility().ordinal()]);
        t.setCollisionRule(collisions[team.getCollision().ordinal()]);
        t.setPrefix(cutTo(team.getPrefix().toLegacyText(), Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13));
        t.setSuffix(cutTo(team.getSuffix().toLegacyText(), Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13));
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        onPacketSend(packet, this);
    }

    /**
     * Sends the packet to the player.
     *
     * @param   packet
     *          Packet to send
     */
    private void sendPacket(@NotNull Packet<?> packet) {
        ((CraftPlayer)player.getPlayer()).getHandle().playerConnection.sendPacket(packet);
    }
}
