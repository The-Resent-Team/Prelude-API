package prelude.adapter;

import org.bukkit.entity.Player;

public abstract class AbstractBukkitPluginMessageSender {
    public abstract void sendPluginMessagePacket(Player player, String channel, Object data);
}
