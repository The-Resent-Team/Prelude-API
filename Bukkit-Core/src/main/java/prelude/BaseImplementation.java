package prelude;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import prelude.adapter.BukkitPlayerAdapter;
import prelude.adapter.VersionAdapter;
import prelude.api.Prelude;
import prelude.mods.BukkitAnchorRenderer;
import prelude.mods.BukkitOffHand;
import prelude.mods.BukkitServerTps;
import prelude.mods.BukkitTotemTweaks;
import prelude.protocol.ProcessedResult;
import prelude.protocol.server.ServerBoundPacket;
import prelude.protocol.server.ServerPacketManager;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * The base Implementation of the ResentAPI for bukkit.
 */
public final class BaseImplementation implements Listener {
    private final PreludePlugin plugin;

    public BaseImplementation(PreludePlugin plugin) {
        this.plugin = plugin;

        Runnable tpsRunnable = () -> {
            Optional<BukkitServerTps> tpsMod = Prelude.getInstance().getMod(BukkitServerTps.class);
            if (!tpsMod.isPresent() || !tpsMod.get().isAllowed() || !tpsMod.get().isOfficiallyHooked()) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                tpsMod.get().sendServerTpsUpdate(BukkitPlayerAdapter.getPreludePlayer(plugin, player), getTPS()[0]);
            }
        };

        Prelude.getInstance().getMod(BukkitServerTps.class).ifPresent((tpsMod) -> {
            if (tpsMod.isOfficiallyHooked()) {
                plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, tpsRunnable, 1L, 20L);
            }
        });


        // Version Adapted Features
        Optional<VersionAdapter> adapterOptional = plugin.getAdapter();
        if (adapterOptional.isPresent()) {
            VersionAdapter adapter = adapterOptional.get();

            // Offhand mod
            if (adapter.hasOffHandSupport()) {
                Prelude.getInstance().getMod(BukkitOffHand.class).ifPresent((offHandMod) -> {
                    if (!offHandMod.isOfficiallyHooked()) {
                        return;
                    }
                    plugin.getServer().getScheduler()
                            .runTaskTimerAsynchronously(plugin, adapter.getOffHandRunnable(offHandMod), 1L, 10L);
                });
            }

            // Totem mod
            if (adapter.hasTotemSupport()) {
                Prelude.getInstance().getMod(BukkitTotemTweaks.class).ifPresent((totemMod) -> {
                    if (!totemMod.isOfficiallyHooked()) {
                        return;
                    }
                    adapter.registerTotemListener(totemMod);
                });
            }

            // Anchor mod
            if (adapter.hasAnchorSupport()) {
                Prelude.getInstance().getMod(BukkitAnchorRenderer.class).ifPresent((anchorMod) -> {
                    if (!anchorMod.isOfficiallyHooked()) {
                        return;
                    }
                    adapter.registerAnchorListener(anchorMod);
                });
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BukkitPlayerAdapter.remove(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BukkitPrelude.getInstance().validateConnection(BukkitPlayerAdapter.getPreludePlayer(plugin, player));
    }

    private double[] getTPS() {
        try {
            Object minecraftServer = getMinecraftServer();
            Field tpsField = minecraftServer.getClass().getDeclaredField("recentTps");
            tpsField.setAccessible(true);
            return (double[]) tpsField.get(minecraftServer);
        } catch (Exception e) {
            PreludePlugin.getInstance().debug(e.getMessage());
            return new double[]{-1, -1, -1};
        }
    }

    private Object getMinecraftServer() throws Exception {
        Object craftServer = Bukkit.getServer();
        Field consoleField = craftServer.getClass().getDeclaredField("console");
        consoleField.setAccessible(true);
        return consoleField.get(craftServer);
    }

    public static class ResentClientMessageListener implements PluginMessageListener {

        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] message) {
            // dump
            PreludePlugin.getInstance().debug("Channel: {}".replace("{}", channel));
            PreludePlugin.getInstance().debug("Player: {}".replace("{}", player.getName()));
            PreludePlugin.getInstance().debug("Message: {}".replace("{}", new String(message)));

            ServerBoundPacket pkt = ServerPacketManager.getServerBoundPacketFromString(new String(message));

            if (pkt == null) {
                PreludePlugin.getInstance().debug("Received message did not correspond to any packet!");
                return;
            }

            ProcessedResult result = pkt.processPacket(Prelude.getServerPacketManager());
            if (result != null) {
                // Do nothing
            }
        }
    }
}
