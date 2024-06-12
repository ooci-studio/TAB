package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.message.RedisMessage;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RedisBelowName extends RedisFeature {

    private final RedisSupport redisSupport;
    @Getter private final BelowName belowName;

    public RedisBelowName(@NotNull RedisSupport redisSupport, @NotNull BelowName belowName) {
        this.redisSupport = redisSupport;
        this.belowName = belowName;
        redisSupport.registerMessage("belowname", Update.class, Update::new);
    }

    @Override
    public void load() {
        belowName.getCustomThread().execute(() -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                redisSupport.sendMessage(new Update(all.getTablistId(), belowName.getValue(all), all.belowNameData.numberFormat.get()));
            }
        }, redisSupport.getFeatureName(), "Resending all Belowname data on reload");
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        for (RedisPlayer redis : redisSupport.getRedisPlayers().values()) {
            player.getScoreboard().setScore(
                    BelowName.OBJECTIVE_NAME,
                    redis.getNickname(),
                    redis.getBelowNameNumber(),
                    null, // Unused by this objective slot
                    redis.getBelowNameFancy()
            );
        }
        belowName.getCustomThread().execute(
                () -> redisSupport.sendMessage(new Update(player.getTablistId(), belowName.getValue(player), player.belowNameData.numberFormat.get())),
                redisSupport.getFeatureName(), "Updating Belowname");
    }

    @Override
    public void onJoin(@NotNull RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().setScore(
                    BelowName.OBJECTIVE_NAME,
                    player.getNickname(),
                    player.getBelowNameNumber(),
                    null, // Unused by this objective slot
                   player.getBelowNameFancy()
            );
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public class Update extends RedisMessage {

        private UUID playerId;
        private int value;
        private String fancyValue;

        @Override
        public void write(@NotNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeInt(value);
            out.writeUTF(fancyValue);
        }

        @Override
        public void read(@NotNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            value = in.readInt();
            fancyValue = in.readUTF();
        }

        @Override
        public void process(@NotNull RedisSupport redisSupport) {
            RedisPlayer target = redisSupport.getRedisPlayers().get(playerId);
            if (target == null) return; // Print warn?
            target.setBelowNameNumber(value);
            target.setBelowNameFancy(belowName.getCache().get(fancyValue));
            onJoin(target);
        }
    }
}
