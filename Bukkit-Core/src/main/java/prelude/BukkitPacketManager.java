package prelude;

import prelude.adapter.BukkitPlayerAdapter;
import prelude.protocol.ProcessedResult;
import prelude.protocol.server.ServerPacketManager;
import prelude.protocol.packets.serverbound.ClientHandshakePacket;
import prelude.protocol.processedresults.serverbound.PreludePlayerInfo;

public class BukkitPacketManager extends ServerPacketManager {
    @Override
    public ProcessedResult processClientHandshake(ClientHandshakePacket packet) {
        PreludePlayerInfo info = packet.getPreludePlayerInfo();
        BukkitPlayerAdapter.registerInfo(info.getUsername(), info);
        return info;
    }
}
