package prelude.api;

import lombok.Getter;
import prelude.protocol.packets.client.ClientBoundPacket;
import prelude.protocol.processedresults.serverbound.PreludePlayerInfo;

import java.util.UUID;

@Getter
@SuppressWarnings("unused")
public abstract class PreludePlayer {
    private final String username;
    private final UUID uuid;
    private final PreludePlayerInfo playerInfo;

    public PreludePlayer(String username, UUID uuid, PreludePlayerInfo preludePlayerInfo) {
        this.username = username;
        this.uuid = uuid;
        this.playerInfo = preludePlayerInfo;
    }

    /**
     * Sends the packet
     * @param packet Outbound Packet to send
     * @author cire3
     * @since 1.0.0
     */
    public abstract void sendPacket(ClientBoundPacket packet);
}
