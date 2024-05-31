package prelude.protocol.packets.client;

import prelude.protocol.PacketManager;
import prelude.protocol.ProcessedResult;

import java.util.regex.Pattern;

// Handlers are all on the client-side, as they require access to the internals of Resent Client
public abstract class ClientBoundPacketHandler<E extends ClientBoundPacket> {
    protected final Pattern pattern;

    protected ClientBoundPacketHandler(String id, Pattern pattern) {
        PacketManager.clientBoundPackets.put(id, this);
        this.pattern = pattern;
    }

    public abstract ProcessedResult handlePacket(E packet);
}
