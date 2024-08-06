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

package com.resentclient.prelude.bukkit;

import org.bukkit.entity.Player;
import com.resentclient.prelude.api.Prelude;
import com.resentclient.prelude.api.PreludePlayer;

import com.resentclient.prelude.adapter.BukkitPlayerAdapter;
import com.resentclient.prelude.protocol.PreludeC2SPacketHandler;
import com.resentclient.prelude.protocol.packets.c2s.*;
import com.resentclient.prelude.protocol.packets.c2s.interactions.*;
import com.resentclient.prelude.protocol.packets.s2c.*;
import com.resentclient.prelude.protocol.packets.s2c.play.*;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

public class BukkitC2SPacketHandler implements PreludeC2SPacketHandler {
    private static Player activePlayer;

    public static void bindPlayer(Player player) {
        activePlayer = player;
    }

    @Override
    public void handleClientHandshake(ClientHandshakePreludeC2SPacket pkt) {
        if (activePlayer == null)
            return;

        PreludePlayer.Info info = new PreludePlayer.Info(pkt.getUsername(), pkt.getResentMajorVersion(),
                pkt.getResentMinorVersion(), pkt.getResentBuildInteger(), pkt.getClientType(), pkt.doesClientClaimSelfIsRankedPlayer(), pkt.getEnabledMods());

        BukkitPlayerAdapter.registerInfo(activePlayer, info);
        try {
            BukkitPlayerAdapter.adapt(PreludePlugin.getInstance().getAdapter(), activePlayer).sendPacket(
                    ServerHandshakePreludeS2CPacket.builder()
                            .preludeMajorVersion(Prelude.MAJOR_VERSION)
                            .preludeMinorVersion(Prelude.MINOR_VERSION)
                            .preludePatchVersion(Prelude.PATCH_VERSION)
                            .serverMajorVersion(VersionUtil.getServerBukkitVersion().getMajor())
                            .serverMinorVersion(VersionUtil.getServerBukkitVersion().getMinor())
                            .serverPatchVersion(VersionUtil.getServerBukkitVersion().getPatch())
                            .build());
            BukkitPlayerAdapter.markSentServerHandshake(activePlayer);
        } catch (IOException e) {
            // ?!?!?! how did this build of prelude-proto even get past tests wtf
        }
    }

    @Override
    public void handleClientAcknowledgeServerHandshake(ClientAcknowledgeServerHandshakePreludeC2SPacket pkt) {
        if (activePlayer == null)
            return;

        if (!BukkitPlayerAdapter.isPlayerAccepted(activePlayer) && BukkitPlayerAdapter.haveSentServerHandshake(activePlayer))
            BukkitPlayerAdapter.markPlayerAccepted(activePlayer);
    }

    @Override
    public void handleEquipOffhand(EquipOffhandPreludeC2SPacket pkt) {
        if (activePlayer == null)
            return;

        PreludePlugin.getInstance().getAdapter().equipSlotToOffhand(activePlayer, pkt.getSlot());
    }

    @Override
    public void handleClientSyncResponse(ClientSyncResponsePreludeC2SPacket pkt) {
        // TODO
    }

    @Override
    public void handleInteractWithOffhand(InteractWithOffhandPreludeC2SPacket pkt) {
        // TODO, make this not-abuseable
    }

    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Override
    public void handleAttemptPlaceInLegacyIllegalSpots(AttemptPlaceInLegacyIllegalSpotsPreludeC2SPacket pkt) {
        // do nothing
    }
}
