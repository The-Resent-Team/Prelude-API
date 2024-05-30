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
    public static final String SERVER_HANDSHAKE_PACKET_FORMAT =
            "{" +
                    "\"prelude-major-version\":\"%prelude-major-version%\"," +
                    "\"prelude-minor-version\":\"%prelude-minor-version%\"," +
                    "\"prelude-patch-version\":\"%prelude-patch-version%\"," +
                    "\"server-major-version\":\"%server-major-version%\"," +
                    "\"server-minor-version\":\"%server-minor-version%\"," +
                    "\"server-patch-version\":\"%server-patch-version%\"" +
            "}";

    public static final String SERVER_HANDSHAKE_PACKET_REGEX =
            "\\{" +
                    "\"prelude-major-version\":\"\\d+\"," +
                    "\"prelude-minor-version\":\"\\d+\"," +
                    "\"prelude-patch-version\":\"\\d+\"," +
                    "\"server-major-version\":\"\\d+\"," +
                    "\"server-minor-version\":\"\\d+\"," +
                    "\"server-patch-version\":\"\\d+\"" +
            "}";

    private int preludeMajorVersion;
    private int preludeMinorVersion;
    private int preludePatchVersion;
    private int serverMajorVersion;
    private int serverMinorVersion;
    private int serverPatchVersion;

    public ServerHandshakePacket(String message) {
        try {
            JsonElement element = JsonParser.parseString(message);
            JsonObject json = element.getAsJsonObject();

            preludeMajorVersion = json.get("prelude-major-version").getAsInt();
            preludeMinorVersion = json.get("prelude-minor-version").getAsInt();
            preludePatchVersion = json.get("prelude-patch-version").getAsInt();
            serverMajorVersion = json.get("server-major-version").getAsInt();
            serverMinorVersion = json.get("server-minor-version").getAsInt();
            serverPatchVersion = json.get("server-patch-version").getAsInt();
        } catch (Exception e) {
            preludeMajorVersion = -1;
            preludeMinorVersion = -1;
            preludePatchVersion = -1;
            serverMajorVersion = -1;
            serverMinorVersion = -1;
            serverPatchVersion = -1;
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
        return Pattern.compile(SERVER_HANDSHAKE_PACKET_REGEX, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerHandshakePacket)) return false;
        ServerHandshakePacket that = (ServerHandshakePacket) o;
        return preludeMajorVersion == that.preludeMajorVersion && preludeMinorVersion == that.preludeMinorVersion && preludePatchVersion == that.preludePatchVersion;
    }
}
