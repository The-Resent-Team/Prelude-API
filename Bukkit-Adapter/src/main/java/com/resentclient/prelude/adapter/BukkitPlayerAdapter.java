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
 *
 */

package com.resentclient.prelude.adapter;

import org.bukkit.entity.Player;
import com.resentclient.prelude.api.Prelude;
import com.resentclient.prelude.api.PreludePlayer;

import java.io.IOException;
import java.util.*;

public final class BukkitPlayerAdapter {
    public static final PreludePlayer NON_RESENT_CLIENT_PLAYER =
            new PreludePlayer(null, null, PreludePlayer.Info.UNKNOWN_INFO) {
                @Override
                public void sendBytes(byte[] bytes) throws IOException {

                }
    };

    private static final Map<Player, PreludePlayer> MAP = new HashMap<>();
    private static final Map<Player, PreludePlayer.Info> INFO = new HashMap<>();
    private static final Set<Player> VERIFIED_PLAYERS = new HashSet<>();
    private static final Map<Player, byte[]> HAS_SENT_VERIFICATION_REQUESTS = new HashMap<>();
    private static final Set<Player> PLAYERS_WHO_TRIED_TO_HANG_PRELUDE = new HashSet<>();

    public static PreludePlayer adapt(VersionAdapter adapter, Player player) {
        if (MAP.containsKey(player))
            return MAP.get(player);

        if (INFO.containsKey(player)) {
            PreludePlayer preludePlayer = new PreludePlayer(player.getName(), player.getUniqueId(), INFO.get(player)) {
                @Override
                public void sendBytes(byte[] bytes) throws IOException {
                    adapter.getMessageSender().sendPluginMessagePacket(player, Prelude.CHANNEL, bytes);
                }
            };

            MAP.put(player, preludePlayer);
            INFO.remove(player);

            return preludePlayer;
        }

        return NON_RESENT_CLIENT_PLAYER;
    }

    public static boolean didPlayerTryToHang(Player player) {
        return PLAYERS_WHO_TRIED_TO_HANG_PRELUDE.contains(player);
    }

    public static void markPlayerTriedToHang(Player player) {
        PLAYERS_WHO_TRIED_TO_HANG_PRELUDE.add(player);
    }

    public static boolean hasSentPlayerVerification(Player player) {
        return HAS_SENT_VERIFICATION_REQUESTS.containsKey(player);
    }

    public static byte[] getPlayerPayload(Player player) {
        return HAS_SENT_VERIFICATION_REQUESTS.get(player);
    }

    public static void markSentPlayerVerification(Player player, byte[] payload) {
        HAS_SENT_VERIFICATION_REQUESTS.put(player, payload);
    }

    public static boolean isPlayerVerified(Player player) {
        return VERIFIED_PLAYERS.contains(player);
    }

    public static void verifyPlayer(Player player) {
        VERIFIED_PLAYERS.add(player);
        HAS_SENT_VERIFICATION_REQUESTS.remove(player);
    }

    public static void registerInfo(Player player, PreludePlayer.Info _info) {
        if (player == null)
            return;

        if (!MAP.containsKey(player))
            INFO.put(player, _info);
    }

    public static void remove(Player player) {
        MAP.remove(player);
        INFO.remove(player);
        VERIFIED_PLAYERS.remove(player);
        HAS_SENT_VERIFICATION_REQUESTS.remove(player);
        PLAYERS_WHO_TRIED_TO_HANG_PRELUDE.remove(player);
    }
}