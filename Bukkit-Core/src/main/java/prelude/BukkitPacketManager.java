package prelude;

import prelude.adapter.BukkitPlayerAdapter;
import prelude.api.PreludePlayer;
import prelude.protocol.C2SPacketHandler;
import prelude.protocol.packets.c2s.ClientHandshakePacket;
import prelude.protocol.packets.c2s.EquipOffhandPacket;

public class BukkitPacketManager extends C2SPacketHandler {
    @Override
    public void handleClientHandshake(ClientHandshakePacket pkt) {
        PreludePlayer.Info info = new PreludePlayer.Info(pkt.getUsername(), pkt.getResentMajorVersion(),
                pkt.getResentMinorVersion(), pkt.getResentBuildInteger(), pkt.getClientType(), pkt.doesClientClaimSelfIsRankedPlayer(), pkt.getEnabledMods());

        BukkitPlayerAdapter.registerInfo(info.username, info);
    }

    @Override
    public void handleEquipOffhand(EquipOffhandPacket equipOffhandPacket) {
        // TODO
    }
}
