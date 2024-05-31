package prelude.protocol;

import prelude.protocol.packets.client.ClientBoundPacketHandler;
import prelude.protocol.packets.clientbound.*;
import prelude.protocol.packets.serverbound.ClientHandshakePacket;
import prelude.protocol.server.ServerBoundPacket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class PacketManager {
    // we use HashMap so we don't need to loop over
    // all handlers to determine the correct handler,
    // which is O(n). Using a Map and doing containsKey
    // is O(1)
    public static Map<String, ClientBoundPacketHandler> clientBoundPackets = new HashMap<>();
    public static Set<ServerBoundPacket> serverBoundPackets = new HashSet<>();

    static {
        // Register SERVER-BOUND packets
        new ClientHandshakePacket();

        // CLIENT-BOUND packet handlers are registered on the Client side,
        // since they require access to Resent Client internals.
    }
}
