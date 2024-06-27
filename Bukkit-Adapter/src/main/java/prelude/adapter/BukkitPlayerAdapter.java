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

package prelude.adapter;

import org.bukkit.entity.Player;
import prelude.api.Prelude;
import prelude.api.PreludePlayer;
import prelude.protocol.S2CPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class BukkitPlayerAdapter {
    public static final PreludePlayer NON_RESENT_CLIENT_PLAYER =
            new PreludePlayer(null, null, PreludePlayer.Info.UNKNOWN_INFO) {
                @Override
                public void sendPacket(S2CPacket packet) {

                }
    };

    private static final Map<Player, PreludePlayer> map = new HashMap<>();
    private static final Map<Player, PreludePlayer.Info> info = new HashMap<>();

    public static PreludePlayer adapt(VersionAdapter adapter, Player player) {
        if (map.containsKey(player))
            return map.get(player);

        if (info.containsKey(player)) {
            PreludePlayer preludePlayer = new PreludePlayer(player.getName(), player.getUniqueId(), info.get(player.getName().toLowerCase())) {
                @Override
                public void sendPacket(S2CPacket packet) throws IOException {
                    adapter.getMessageSender().sendPluginMessagePacket(player, Prelude.CHANNEL, packet.toBytes());
                }
            };

            map.put(player, preludePlayer);
            info.remove(player);

            return preludePlayer;
        }

        return NON_RESENT_CLIENT_PLAYER;
    }

    public static void registerInfo(Player player, PreludePlayer.Info _info) {
        if (player == null)
            return;

        if (!map.containsKey(player))
            info.put(player, _info);
    }

    public static void remove(Player player) {
        map.remove(player);
        info.remove(player);
    }
}