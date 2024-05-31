package prelude.protocol.packets.client;

import com.google.gson.JsonParser;
import prelude.protocol.PacketManager;
import prelude.protocol.ProcessedResult;
import prelude.protocol.packets.clientbound.ServerHandshakePacket;

@SuppressWarnings("unused")
public abstract class ClientPacketManager extends PacketManager {
    public abstract ProcessedResult processServerHandshake(ServerHandshakePacket packet);

    public static ClientBoundPacketHandler getClientBoundPacketHandlerFromString(String string) {
        String receiver = JsonParser.parseString(string).getAsJsonObject().get("packet_receiver").getAsString();

        if (clientBoundPackets.containsKey(receiver))
            return clientBoundPackets.get(receiver);

        return null;
    }
}
