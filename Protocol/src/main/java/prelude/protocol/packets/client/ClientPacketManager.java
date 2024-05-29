package prelude.protocol.packets.client;

import com.google.gson.JsonParser;
import prelude.protocol.PacketManager;
import prelude.protocol.ProcessedResult;
import prelude.protocol.packets.clientbound.ServerHandshakePacket;

@SuppressWarnings("unused")
public abstract class ClientPacketManager extends PacketManager {
    public abstract ProcessedResult processServerHandshake(ServerHandshakePacket packet);

    public static ClientBoundPacketHandler getClientBoundPacketHandlerFromString(String string) {
        String message = JsonParser.parseString(string).getAsJsonObject().get("message").getAsString();

        for (ClientBoundPacketHandler handler : clientBoundPackets)
            if (handler.canHandlePacket(message))
                return handler;

        return null;
    }
}
