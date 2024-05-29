package prelude.adapter;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import prelude.api.Prelude;
import prelude.api.PreludePlayer;
import prelude.protocol.packets.client.ClientBoundPacket;
import prelude.protocol.processedresults.serverbound.PreludePlayerInfo;

import java.util.HashMap;
import java.util.Map;

public final class BukkitPlayerAdapter {
    private static final PreludePlayer NON_RESENT_CLIENT_PLAYER =
            new PreludePlayer(null, null, PreludePlayerInfo.UNKNOWN_INFO) {
                @Override
                public void sendPacket(ClientBoundPacket packet) {

                }
    };

    private static final Map<Player, PreludePlayer> map = new HashMap<>();
    private static final Map<String, PreludePlayerInfo> info = new HashMap<>();

    public static PreludePlayer getPreludePlayer(JavaPlugin plugin, Player player) {
        if (map.containsKey(player))
            return map.get(player);

        if (info.containsKey(player.getName().toLowerCase())) {
            PreludePlayer preludePlayer = new PreludePlayer(player.getName(), player.getUniqueId(), info.get(player.getName().toLowerCase())) {
                @Override
                public void sendPacket(ClientBoundPacket packet) {
                    player.sendPluginMessage(
                            plugin,
                            Prelude.CHANNEL,
                            packet.serialize().getBytes()
                    );
                }
            };

            map.put(player, preludePlayer);
            info.remove(player.getName().toLowerCase());

            return preludePlayer;
        }

        return NON_RESENT_CLIENT_PLAYER;
    }

    public static void registerInfo(String player, PreludePlayerInfo preludePlayerInfo) {
        info.put(player.toLowerCase().trim(), preludePlayerInfo);
    }

    public static void remove(Player player) {
        map.remove(player);
    }
}