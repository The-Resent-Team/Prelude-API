package prelude.protocol;

import prelude.protocol.packets.client.ClientBoundPacketHandler;
import prelude.protocol.packets.clientbound.*;
import prelude.protocol.packets.serverbound.ClientHandshakePacket;
import prelude.protocol.server.ServerBoundPacket;

import java.util.HashSet;
import java.util.Set;

public abstract class PacketManager {
    public static Set<ClientBoundPacketHandler> clientBoundPackets = new HashSet<>();
    public static Set<ServerBoundPacket> serverBoundPackets = new HashSet<>();

    static {
        // Register SERVER-BOUND packets
        new ClientHandshakePacket();

        // CLIENT-BOUND packet handlers are registered on the Client side,
        // since they require access to Resent Client internals.
    }
}
