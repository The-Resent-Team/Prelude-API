package prelude.protocol.packets.client;

import prelude.protocol.Packet;
import prelude.protocol.ProcessedResult;

public abstract class ClientBoundPacket extends Packet {
    public static final String GENERIC_PACKET_FORMAT =
            "{" +
                    "\"packet_receiver\":\"%packet_receiver%\"," +
                    "\"message\":\"%message%\"" +
            "}";

    public abstract String serialize();

    public abstract ProcessedResult processPacket(ClientPacketManager manager);

    public abstract ClientBoundPacket createNewInstanceWithData(String data);
}