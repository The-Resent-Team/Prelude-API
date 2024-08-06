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

package com.resentclient.prelude;

import com.resentclient.prelude.protocol.packets.c2s.ClientAcknowledgeServerHandshakePreludeC2SPacket;
import com.resentclient.prelude.protocol.packets.c2s.ClientSyncResponsePreludeC2SPacket;
import com.resentclient.prelude.protocol.packets.c2s.interactions.AttemptPlaceInLegacyIllegalSpotsPreludeC2SPacket;
import com.resentclient.prelude.protocol.packets.c2s.interactions.InteractWithOffhandPreludeC2SPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.resentclient.prelude.adapter.BukkitPlayerAdapter;
import com.resentclient.prelude.api.PreludePlayer;
import com.resentclient.prelude.protocol.PreludeC2SPacketHandler;
import com.resentclient.prelude.protocol.packets.c2s.ClientHandshakePreludeC2SPacket;
import com.resentclient.prelude.protocol.packets.c2s.EquipOffhandPreludeC2SPacket;

public class BukkitC2SPacketHandler implements PreludeC2SPacketHandler {
    private static Player activePlayer;

    public static void bindPlayer(Player player) {
        activePlayer = player;
    }

    @Override
    public void handleClientHandshake(ClientHandshakePreludeC2SPacket pkt) {
        PreludePlayer.Info info = new PreludePlayer.Info(pkt.getUsername(), pkt.getResentMajorVersion(),
                pkt.getResentMinorVersion(), pkt.getResentBuildInteger(), pkt.getClientType(), pkt.doesClientClaimSelfIsRankedPlayer(), pkt.getEnabledMods());

        BukkitPlayerAdapter.registerInfo(Bukkit.getPlayer(info.username), info);
    }

    @Override
    public void handleClientAcknowledgeServerHandshake(ClientAcknowledgeServerHandshakePreludeC2SPacket pkt) {

    }

    @Override
    public void handleEquipOffhand(EquipOffhandPreludeC2SPacket pkt) {
        PreludePlugin.getInstance().getAdapter().equipSlotToOffhand(activePlayer, pkt.getSlot());
    }

    @Override
    public void handleClientSyncResponse(ClientSyncResponsePreludeC2SPacket pkt) {

    }

    @Override
    public void handleInteractWithOffhand(InteractWithOffhandPreludeC2SPacket pkt) {

    }

    @Override
    public void handleAttemptPlaceInLegacyIllegalSpots(AttemptPlaceInLegacyIllegalSpotsPreludeC2SPacket pkt) {

    }
}
