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

    private static final Map<Player, PreludePlayer> PRELUDE_PLAYER_CACHE = new HashMap<>();
    private static final Map<Player, PreludePlayer.Info> PRELUDE_PLAYER_INFO = new HashMap<>();

    private static final Set<Player> VERIFIED_PLAYERS = new HashSet<>();
    private static final Map<Player, byte[]> SENT_VERIFICATION_REQUESTS = new HashMap<>();

    private static final Set<Player> HAVE_SENT_HANDSHAKE_PLAYERS = new HashSet<>();
    private static final Set<Player> ACCEPTED_HANDSHAKE_PLAYERS = new HashSet<>();

    private static final Set<Player> PLAYERS_WHO_TRIED_TO_HANG_PRELUDE = new HashSet<>();

    public static PreludePlayer adapt(VersionAdapter adapter, Player player) {
        if (PRELUDE_PLAYER_CACHE.containsKey(player))
            return PRELUDE_PLAYER_CACHE.get(player);

        if (PRELUDE_PLAYER_INFO.containsKey(player)) {
            PreludePlayer preludePlayer = new PreludePlayer(player.getName(), player.getUniqueId(), PRELUDE_PLAYER_INFO.get(player)) {
                @Override
                public void sendBytes(byte[] bytes) throws IOException {
                    adapter.getMessageSender().sendPluginMessagePacket(player, Prelude.CHANNEL, bytes);
                }
            };

            PRELUDE_PLAYER_CACHE.put(player, preludePlayer);
            PRELUDE_PLAYER_INFO.remove(player);

            return preludePlayer;
        }

        return NON_RESENT_CLIENT_PLAYER;
    }

    // setters (real)
    public static void markPlayerTriedToHang(Player player) {
        PLAYERS_WHO_TRIED_TO_HANG_PRELUDE.add(player);
    }

    public static void markSentPlayerVerification(Player player, byte[] payload) {
        SENT_VERIFICATION_REQUESTS.put(player, payload);
    }

    public static void markPlayerAccepted(Player player) {
        ACCEPTED_HANDSHAKE_PLAYERS.add(player);
    }

    public static void markPlayerVerified(Player player) {
        VERIFIED_PLAYERS.add(player);
        SENT_VERIFICATION_REQUESTS.remove(player);
    }

    public static void markSentServerHandshake(Player player) {
        HAVE_SENT_HANDSHAKE_PLAYERS.add(player);
    }

    // getters (not real)
    public static boolean isPlayerVerified(Player player) {
        return VERIFIED_PLAYERS.contains(player);
    }

    public static boolean didPlayerTryToHang(Player player) {
        return PLAYERS_WHO_TRIED_TO_HANG_PRELUDE.contains(player);
    }

    public static byte[] getPlayerPayload(Player player) {
        return SENT_VERIFICATION_REQUESTS.get(player);
    }

    public static boolean isPlayerAccepted(Player player) {
        return ACCEPTED_HANDSHAKE_PLAYERS.contains(player);
    }

    public static boolean haveSentServerHandshake(Player player) {
        return HAVE_SENT_HANDSHAKE_PLAYERS.contains(player);
    }

    // other stuff
    public static void registerInfo(Player player, PreludePlayer.Info _info) {
        if (player == null)
            return;

        if (!PRELUDE_PLAYER_CACHE.containsKey(player))
            PRELUDE_PLAYER_INFO.put(player, _info);
    }

    public static void remove(Player player) {
        PRELUDE_PLAYER_CACHE.remove(player);
        PRELUDE_PLAYER_INFO.remove(player);
        VERIFIED_PLAYERS.remove(player);
        ACCEPTED_HANDSHAKE_PLAYERS.remove(player);
        SENT_VERIFICATION_REQUESTS.remove(player);
        HAVE_SENT_HANDSHAKE_PLAYERS.remove(player);
        PLAYERS_WHO_TRIED_TO_HANG_PRELUDE.remove(player);
    }
}