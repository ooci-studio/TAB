package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor
public class PlayerJoin extends ProxyMessage {

    @Getter private ProxyPlayer decodedPlayer;
    private TabPlayer encodedPlayer;

    public PlayerJoin(@NotNull TabPlayer encodedPlayer) {
        this.encodedPlayer = encodedPlayer;
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, encodedPlayer.getTablistId());
        out.writeUTF(encodedPlayer.getName());
        out.writeUTF(encodedPlayer.server);
        out.writeBoolean(encodedPlayer.isVanished());
        out.writeBoolean(encodedPlayer.hasPermission(TabConstants.Permission.STAFF));
        TabList.Skin skin = encodedPlayer.getTabList().getSkin();
        out.writeBoolean(skin != null);

        // Load skin immediately to make global playerlist stuff not too complicated
        if (skin != null) {
            out.writeUTF(skin.getValue());
            out.writeBoolean(skin.getSignature() != null);
            if (skin.getSignature() != null) {
                out.writeUTF(skin.getSignature());
            }
        }
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        UUID uniqueId = readUUID(in);
        String name = in.readUTF();
        String server = in.readUTF();
        boolean vanished = in.readBoolean();
        boolean staff = in.readBoolean();
        decodedPlayer = new ProxyPlayer(uniqueId, name, name, server, vanished, staff);

        // Load skin immediately to make global playerlist stuff not too complicated
        if (in.readBoolean()) {
            String value = in.readUTF();
            String signature = null;
            if (in.readBoolean()) {
                signature = in.readUTF();
            }
            decodedPlayer.setSkin(new TabList.Skin(value, signature));
        }
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        TAB.getInstance().debug("Processing join of proxy player " + decodedPlayer.getName() + " (" + decodedPlayer.getUniqueId() + ")");
        // Do not create duplicated player
        if (TAB.getInstance().isPlayerConnected(decodedPlayer.getUniqueId())) {
            TAB.getInstance().debug("The player " + decodedPlayer.getName() + " is already connected");
            return;
        }
        proxySupport.getProxyPlayers().put(decodedPlayer.getUniqueId(), decodedPlayer);
        TAB.getInstance().getFeatureManager().onJoin(decodedPlayer);
    }
}
