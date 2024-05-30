package prelude.protocol.packets.serverbound;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import prelude.protocol.PacketManager;
import prelude.protocol.ProcessedResult;
import prelude.protocol.server.ServerBoundPacket;
import prelude.protocol.server.ServerPacketManager;
import prelude.protocol.processedresults.serverbound.PreludePlayerInfo;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public final class ClientHandshakePacket extends ServerBoundPacket {
    public static final String CLIENT_HANDSHAKE_PACKET_FORMAT =
            "{" +
                    "\"username\":\"%username%\"," +
                    "\"resent-version\":\"%resVer%\"," +
                    "\"patch-num\":\"%patchNum%\"," +
                    "\"client-type\":\"%clientType%\"," +
                    "\"is-ranked-player\":\"%isRankedPlayer%\"," +
                    "\"enabled-mods\":\"%modsOn%\"" +
            "}";

    public static final String CLIENT_HANDSHAKE_PACKET_REGEX =
            "\\{" +
                    "\"username\":\".+\"," +
                    "\"resent-version\":\"\\d+\\.\\d+\"," +
                    "\"patch-num\":\"\\d+\"," +
                    "\"client-type\":\".+\"," +
                    "\"is-ranked-player\":\".+\"," +
                    "\"enabled-mods\":\".+\"" +
            "}";

    private final PreludePlayerInfo preludePlayerInfo;

    public ClientHandshakePacket() {
        preludePlayerInfo = PreludePlayerInfo.UNKNOWN_INFO;
        PacketManager.serverBoundPackets.add(this);
    }

    public ClientHandshakePacket(String message) {
        PreludePlayerInfo result;

        try {
            JsonElement element = JsonParser.parseString(message);
            JsonObject json = element.getAsJsonObject();
            result = new PreludePlayerInfo(
                    json.get("username").getAsString(),
                    json.get("resent-version").getAsString(),
                    json.get("patch-num").getAsString(),
                    json.get("client-type").getAsString(),
                    Boolean.parseBoolean(json.get("is-ranked-player").getAsString()),
                    json.get("enabled-mods").getAsString().split(",")
            );
        } catch (Exception e) {
            result = PreludePlayerInfo.UNKNOWN_INFO;
        }

        preludePlayerInfo = result;
    }

    @Override
    public ProcessedResult processPacket(ServerPacketManager manager) {
        return manager.processClientHandshake(this);
    }

    @Override
    public ClientHandshakePacket createNewInstanceWithData(String data) {
        return new ClientHandshakePacket(data);
    }

    @Override
    public Pattern getPattern() {
        return Pattern.compile(CLIENT_HANDSHAKE_PACKET_REGEX, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientHandshakePacket)) return false;
        ClientHandshakePacket that = (ClientHandshakePacket) o;
        return Objects.equals(preludePlayerInfo, that.preludePlayerInfo);
    }
}