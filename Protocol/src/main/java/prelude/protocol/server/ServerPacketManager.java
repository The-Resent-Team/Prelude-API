package prelude.protocol.server;

import com.google.gson.JsonParser;
import prelude.protocol.PacketManager;
import prelude.protocol.ProcessedResult;
import prelude.protocol.packets.serverbound.ClientHandshakePacket;

public abstract class ServerPacketManager extends PacketManager {
    public abstract ProcessedResult processClientHandshake(ClientHandshakePacket info);

    public static ServerBoundPacket getServerBoundPacketFromString(String string) {
        String message = JsonParser.parseString(string).getAsJsonObject().get("message").getAsString();

        for (ServerBoundPacket packet : serverBoundPackets)
            if (packet.getPattern().matcher(message.trim().toLowerCase()).matches())
                return packet.createNewInstanceWithData(message);

        return null;
    }
}
