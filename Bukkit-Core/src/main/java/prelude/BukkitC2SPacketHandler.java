package prelude;

import org.bukkit.entity.Player;
import prelude.adapter.BukkitPlayerAdapter;
import prelude.api.PreludePlayer;
import prelude.protocol.C2SPacketHandler;
import prelude.protocol.packets.c2s.ClientHandshakePacket;
import prelude.protocol.packets.c2s.EquipOffhandPacket;

public class BukkitC2SPacketHandler extends C2SPacketHandler {
    private static Player activePlayer;

    @Override
    public void handleClientHandshake(ClientHandshakePacket pkt) {
        PreludePlayer.Info info = new PreludePlayer.Info(pkt.getUsername(), pkt.getResentMajorVersion(),
                pkt.getResentMinorVersion(), pkt.getResentBuildInteger(), pkt.getClientType(), pkt.doesClientClaimSelfIsRankedPlayer(), pkt.getEnabledMods());

        BukkitPlayerAdapter.registerInfo(info.username, info);
    }

    @Override
    public void handleEquipOffhand(EquipOffhandPacket equipOffhandPacket) {
        PreludePlugin.getInstance().getAdapter().equipSlotToOffhand(activePlayer, equipOffhandPacket.getSlot());
    }

    public static void bindPlayer(Player player) {
        activePlayer = player;
    }
}
