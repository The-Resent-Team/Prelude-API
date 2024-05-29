package prelude.protocol.packets.clientbound;

import lombok.Builder;
import prelude.protocol.packets.client.ClientBoundPacket;
import prelude.protocol.packets.client.ClientPacketManager;
import prelude.protocol.PacketManager;
import prelude.protocol.ProcessedResult;

import java.util.Objects;
import java.util.regex.Pattern;

@Builder
public class AnchorRendererPacket extends ClientBoundPacket {
    public static final String ANCHOR_RENDERER_PACKET_MESSAGE_FORMAT =
            "{" +
                    "\"x\":\"%x%\"," +
                    "\"y\":\"%y%\"," +
                    "\"z\":\"%z%\"," +
                    "\"charge\":\"%charge%\"" +
            "}";

    public static final String ANCHOR_RENDERER_PACKET_MESSAGE_REGEX =
            "\\{" +
                    "\"x\":\"\\d+\"," +
                    "\"y\":\"\\d+\"," +
                    "\"z\":\"\\d+\"," +
                    "\"charge\":\"[0-5]\"" +
            "}";

    private int x;
    private int y;
    private int z;
    private int charge;
    private String receiver;

    @Override
    public String serialize() {
        return GENERIC_PACKET_FORMAT
                .replace("%receiver%", receiver)
                .replace("%message%",
                        ANCHOR_RENDERER_PACKET_MESSAGE_FORMAT
                                .replace("%x%", Integer.toString(x))
                                .replace("%y%", Integer.toString(y))
                                .replace("%z%", Integer.toString(z))
                                .replace("%charge%", Integer.toString(charge))
                );
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
    public Pattern getPattern() {
        return Pattern.compile(ANCHOR_RENDERER_PACKET_MESSAGE_REGEX, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnchorRendererPacket)) return false;
        AnchorRendererPacket that = (AnchorRendererPacket) o;
        return x == that.x && y == that.y && z == that.z && charge == that.charge && Objects.equals(receiver, that.receiver);
    }
}