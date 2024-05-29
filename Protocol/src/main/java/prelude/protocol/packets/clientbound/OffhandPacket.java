package prelude.protocol.packets.clientbound;

import lombok.Builder;
import prelude.protocol.packets.client.ClientBoundPacket;
import prelude.protocol.packets.client.ClientPacketManager;
import prelude.protocol.PacketManager;
import prelude.protocol.ProcessedResult;

import java.util.Objects;
import java.util.regex.Pattern;

@Builder
public class OffhandPacket extends ClientBoundPacket {
    public static final String OFFHAND_PACKET_MESSAGE_FORMAT =
            "{" +
                    "\"action\":\"equip_item\"," +
                    "\"item_id\":\"%item_id%\"," +
                    "\"enchanted\":\"%enchanted%\"" +
            "}";

    private String action;
    private String itemId;
    private boolean enchanted;
    private String receiver;

    @Override
    public String serialize() {
        return GENERIC_PACKET_FORMAT
                .replace("%receiver%", receiver)
                .replace("%message%", OFFHAND_PACKET_MESSAGE_FORMAT
                        .replace("%action%", action)
                        .replace("%item_id%", itemId)
                        .replace("%enchanted%", String.valueOf(enchanted))
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
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OffhandPacket)) return false;
        OffhandPacket that = (OffhandPacket) o;
        return enchanted == that.enchanted && Objects.equals(action, that.action) && Objects.equals(itemId, that.itemId)
                && Objects.equals(receiver, that.receiver);
    }
}
