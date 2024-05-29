package prelude.protocol.processedresults.serverbound;

import lombok.Getter;
import prelude.protocol.ProcessedResult;
import prelude.protocol.packets.serverbound.ClientHandshakePacket;

import java.util.Arrays;
import java.util.Objects;

@Getter
@SuppressWarnings("unused")
public final class PreludePlayerInfo extends ProcessedResult {
    public static final PreludePlayerInfo UNKNOWN_INFO = new PreludePlayerInfo(null, null, null, null, false, null);

    private final String username;
    private final String resentVersion;
    private final String resentPatchNum;
    private final String clientType;
    private final boolean rankedPlayer;
    private final String[] enabledMods;

    public PreludePlayerInfo(String username, String resentVersion, String resentPatchNum, String clientType, boolean isRankedPlayer, String[] enabledMods) {
        this.username = username;
        this.resentVersion = resentVersion;
        this.resentPatchNum = resentPatchNum;
        this.clientType = clientType;
        this.rankedPlayer = isRankedPlayer;
        this.enabledMods = enabledMods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreludePlayerInfo)) return false;
        PreludePlayerInfo that = (PreludePlayerInfo) o;
        return rankedPlayer == that.rankedPlayer && Objects.equals(resentVersion, that.resentVersion) &&
                Objects.equals(resentPatchNum, that.resentPatchNum) && Objects.equals(clientType, that.clientType) &&
                Objects.deepEquals(enabledMods, that.enabledMods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resentVersion, resentPatchNum, clientType, rankedPlayer, Arrays.hashCode(enabledMods));
    }

    @Override
    public String toString() {
        return "PreludePlayerInfo:" + ClientHandshakePacket.HANDSHAKE_PACKET_FORMAT
                        .replace("%resVer%", resentVersion)
                        .replace("%patchNum%", resentPatchNum)
                        .replace("%clientType%", clientType)
                        .replace("%isRankedPlayer%", rankedPlayer + "")
                        .replace("%modsOn%",
                                Arrays.toString(enabledMods).substring(1, enabledMods.length - 1));
}
}
