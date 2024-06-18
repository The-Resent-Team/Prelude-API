package prelude.adapter;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import prelude.api.Prelude;
import prelude.api.PreludePlayer;
import prelude.protocol.S2CPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class BukkitPlayerAdapter {
    private static final PreludePlayer NON_RESENT_CLIENT_PLAYER =
            new PreludePlayer(null, null, PreludePlayer.Info.UNKNOWN_INFO) {
                @Override
                public void sendPacket(S2CPacket packet) {

                }
    };

    private static final Map<Player, PreludePlayer> map = new HashMap<>();
    private static final Map<String, PreludePlayer.Info> info = new HashMap<>();

    public static PreludePlayer getPreludePlayer(JavaPlugin plugin, Player player) {
        if (map.containsKey(player))
            return map.get(player);

        if (info.containsKey(player.getName().toLowerCase())) {
            PreludePlayer preludePlayer = new PreludePlayer(player.getName(), player.getUniqueId(), info.get(player.getName().toLowerCase())) {
                @Override
                public void sendPacket(S2CPacket packet) throws IOException {
                    player.sendPluginMessage(
                            plugin,
                            Prelude.CHANNEL,
                            packet.toBytes()
                    );
                }
            };

            map.put(player, preludePlayer);
            info.remove(player.getName().toLowerCase());

            return preludePlayer;
        }

        return NON_RESENT_CLIENT_PLAYER;
    }

    public static void registerInfo(String player, PreludePlayer.Info _info) {
        info.put(player.toLowerCase().trim(), _info);
    }

    public static void remove(Player player) {
        map.remove(player);
    }
}