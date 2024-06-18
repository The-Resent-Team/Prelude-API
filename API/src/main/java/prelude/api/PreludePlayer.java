package prelude.api;

import lombok.Getter;
import prelude.protocol.S2CPacket;
import prelude.protocol.packets.c2s.ClientHandshakePacket;

import java.io.IOException;
import java.util.UUID;

@Getter
@SuppressWarnings("unused")
public abstract class PreludePlayer {
    private final String username;
    private final UUID uuid;
    private final Info info;

    public PreludePlayer(String username, UUID uuid, Info info) {
        this.username = username;
        this.uuid = uuid;
        this.info = info;
    }

    /**
     * Sends the packet
     * @param packet S2CPacket to send
     * @author cire3
     * @since 1.0.0
     */
    public abstract void sendPacket(S2CPacket packet) throws IOException;

    public static class Info {
        public static Info UNKNOWN_INFO = new Info(null, -1, -1, -1, null, false, null);

        public final String username;
        public final int resentMajorVersion;
        public final int resentMinorVersion;
        public final int resentBuildInteger;
        public final ClientHandshakePacket.ClientType clientType;
        public final boolean clientClaimsSelfIsRankedPlayer;
        public final String[] enabledMods;

        public Info(String username, int resentMajorVersion, int resentMinorVersion, int resentBuildInteger, ClientHandshakePacket.ClientType clientType, boolean clientClaimsSelfIsRankedPlayer, String[] enabledMods) {
            this.username = username;
            this.resentMajorVersion = resentMajorVersion;
            this.resentMinorVersion = resentMinorVersion;
            this.resentBuildInteger = resentBuildInteger;
            this.clientType = clientType;
            this.clientClaimsSelfIsRankedPlayer = clientClaimsSelfIsRankedPlayer;
            this.enabledMods = enabledMods;
        }
    }
}
