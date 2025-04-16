package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.TextHolder;
import com.velocitypowered.api.scoreboard.*;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Scoreboard implementation using VelocityScoreboardAPI plugin.
 */
public class VelocityScoreboard extends SafeScoreboard<VelocityTabPlayer> {

    private static final Function<TabComponent, TextHolder> textHolderFunction =
            component -> TextHolder.of(component.toLegacyText(), component.toAdventure());

    private static final StringToComponentCache displayNames = new StringToComponentCache("Team display name", 5000);

    private static final TeamColor[] colors = TeamColor.values();
    private static final com.velocitypowered.api.scoreboard.NameVisibility[] visibilities = com.velocitypowered.api.scoreboard.NameVisibility.values();
    private static final com.velocitypowered.api.scoreboard.CollisionRule[] collisions = com.velocitypowered.api.scoreboard.CollisionRule.values();
    private final ProxyScoreboard scoreboard;

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player to send scoreboard to
     */
    public VelocityScoreboard(@NotNull VelocityTabPlayer player) {
        super(player);
        scoreboard = ScoreboardManager.getInstance().getProxyScoreboard(player.getPlayer());
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        try {
            ProxyObjective.Builder builder = scoreboard.objectiveBuilder(objective.getName())
                    .healthDisplay(com.velocitypowered.api.scoreboard.HealthDisplay.valueOf(objective.getHealthDisplay().name()))
                    .title(objective.getTitle().toTextHolder(textHolderFunction))
                    .numberFormat(objective.getNumberFormat() == null ? null : NumberFormat.fixed(objective.getNumberFormat().toAdventure()));
            objective.setPlatformObjective(scoreboard.registerObjective(builder));
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to register objective " + objective.getName() + " for player " + player.getName(), e);
        }
    }

    @Override
    public void setDisplaySlot(@NonNull Objective objective) {
        ((ProxyObjective)objective.getPlatformObjective()).setDisplaySlot(com.velocitypowered.api.scoreboard.DisplaySlot.valueOf(objective.getDisplaySlot().name()));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        try {
            scoreboard.unregisterObjective(objective.getName());
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to unregister objective " + objective.getName() + " for player " + player.getName(), e);
        }
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        try {
            ProxyObjective obj = (ProxyObjective) objective.getPlatformObjective();
            obj.setHealthDisplay(com.velocitypowered.api.scoreboard.HealthDisplay.valueOf(objective.getHealthDisplay().name()));
            obj.setTitle(objective.getTitle().toTextHolder(textHolderFunction));
            obj.setNumberFormat(objective.getNumberFormat() == null ? null : NumberFormat.fixed(objective.getNumberFormat().toAdventure()));
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to update objective " + objective.getName() + " for player " + player.getName(), e);
        }
    }

    @Override
    public void setScore(@NonNull Score score) {
        try {
            ((ProxyObjective)score.getObjective().getPlatformObjective()).setScore(score.getHolder(), b -> b
                    .score(score.getValue())
                    .displayName(score.getDisplayName() == null ? null : score.getDisplayName().toAdventure())
                    .numberFormat(score.getNumberFormat() == null ? null : NumberFormat.fixed(score.getNumberFormat().toAdventure()))
            );
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to set score " + score.getHolder() + " for player " + player.getName(), e);
        }
    }

    @Override
    public void removeScore(@NonNull Score score) {
        try {
            ((ProxyObjective)score.getObjective().getPlatformObjective()).removeScore(score.getHolder());
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to remove score " + score.getHolder() + " for player " + player.getName(), e);
        }
    }

    @Override
    @NotNull
    public Object createTeam(@NonNull String name) {
        return this; // This API does not work that way
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        try {
            team.setPlatformTeam(scoreboard.registerTeam(scoreboard.teamBuilder(team.getName())
                    .displayName(displayNames.get(team.getName()).toTextHolder(textHolderFunction))
                    .prefix(team.getPrefix().toTextHolder(textHolderFunction))
                    .suffix(team.getSuffix().toTextHolder(textHolderFunction))
                    .nameVisibility(visibilities[team.getVisibility().ordinal()])
                    .collisionRule(collisions[team.getCollision().ordinal()])
                    .allowFriendlyFire((team.getOptions() & 0x01) > 0)
                    .canSeeFriendlyInvisibles((team.getOptions() & 0x02) > 0)
                    .color(colors[team.getColor().getLegacyColor().ordinal()])
                    .entries(team.getPlayers())
            ));
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Team " + team.getName() + " already existed with entry " +
                    scoreboard.getTeam(team.getName()).getEntries() + " when registering for player " + player.getName()
                    + " with new entry " + team.getPlayers() + ", unregistering", e);
            unregisterTeam(team);
            registerTeam(team);
        }
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        try {
            scoreboard.unregisterTeam(team.getName());
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Team " + team.getName() + " did not exist when unregistering for player " + player.getName(), e);
        }
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        ((ProxyTeam)team.getPlatformTeam()).updateProperties(b -> b
                .prefix(team.getPrefix().toTextHolder(textHolderFunction))
                .suffix(team.getSuffix().toTextHolder(textHolderFunction))
                .nameVisibility(visibilities[team.getVisibility().ordinal()])
                .collisionRule(collisions[team.getCollision().ordinal()])
                .color(colors[team.getColor().getLegacyColor().ordinal()])
                .allowFriendlyFire((team.getOptions() & 0x01) > 0)
                .canSeeFriendlyInvisibles((team.getOptions() & 0x02) > 0)
        );
    }
}
