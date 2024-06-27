/*
 * Prelude-API is a plugin to implement features for the Client.
 * Copyright (C) 2024 cire3, Preva1l
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package prelude;

import org.bukkit.Bukkit;
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

        BukkitPlayerAdapter.registerInfo(Bukkit.getPlayer(info.username), info);
    }

    @Override
    public void handleEquipOffhand(EquipOffhandPacket equipOffhandPacket) {
        PreludePlugin.getInstance().getAdapter().equipSlotToOffhand(activePlayer, equipOffhandPacket.getSlot());
    }

    public static void bindPlayer(Player player) {
        activePlayer = player;
    }
}
