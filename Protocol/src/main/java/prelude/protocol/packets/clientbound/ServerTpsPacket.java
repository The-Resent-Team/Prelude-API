package prelude.protocol.packets.clientbound;

import lombok.Builder;
import prelude.protocol.packets.client.ClientBoundPacket;
import prelude.protocol.packets.client.ClientPacketManager;
import prelude.protocol.PacketManager;
import prelude.protocol.ProcessedResult;

import java.util.Objects;
import java.util.regex.Pattern;


@Builder
public class ServerTpsPacket extends ClientBoundPacket {
    private double tps;
    private String receiver;

    @Override
    public String serialize() {
        return GENERIC_PACKET_FORMAT
                .replace("%receiver%", receiver)
                .replace("%message%", String.valueOf(tps));
    }

    @Override
    public ProcessedResult processPacket(ClientPacketManager manager) {
        return null;
    }

    @Override
    public ClientBoundPacket createNewInstanceWithData(String data) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerTpsPacket)) return false;
        ServerTpsPacket that = (ServerTpsPacket) o;
        return tps == that.tps && Objects.equals(receiver, that.receiver);
    }
}
