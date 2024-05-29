package prelude.protocol.packets.clientbound;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Builder;
import lombok.Getter;
import prelude.protocol.*;
import prelude.protocol.packets.client.ClientBoundPacket;
import prelude.protocol.packets.client.ClientPacketManager;

import java.util.regex.Pattern;

@Builder
@Getter
public class ServerHandshakePacket extends ClientBoundPacket {
    public static final String HANDSHAKE_PACKET_FORMAT =
            "{" +
                    "\"major-version\":\"%version%\"," +
                    "\"minor-version\":\"%minor-version%\"," +
                    "\"patch-version\":\"%patch-version%\"" +
            "}";

    private int majorVersion;
    private int minorVersion;
    private int patchVersion;

    public ServerHandshakePacket(String message) {
        try {
            JsonElement element = JsonParser.parseString(message);
            JsonObject json = element.getAsJsonObject();

            majorVersion = json.get("major-version").getAsInt();
            minorVersion = json.get("minor-version").getAsInt();
            patchVersion = json.get("patch-version").getAsInt();
        } catch (Exception e) {
            majorVersion = -1;
            minorVersion = -1;
            patchVersion = -1;
        }
    }

    @Override
    public String serialize() {
        return "";
    }

    @Override
    public ProcessedResult processPacket(ClientPacketManager manager) {
        return manager.processServerHandshake(this);
    }

    @Override
    public ServerHandshakePacket createNewInstanceWithData(String data) {
        return new ServerHandshakePacket(data);
    }

    @Override
    public Pattern getPattern() {
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerHandshakePacket)) return false;
        ServerHandshakePacket that = (ServerHandshakePacket) o;
        return majorVersion == that.majorVersion && minorVersion == that.minorVersion && patchVersion == that.patchVersion;
    }
}
