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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import prelude.adapter.BukkitPlayerAdapter;
import prelude.adapter.VersionAdapter;
import prelude.api.Prelude;
import prelude.mods.BukkitAnchorRenderer;
import prelude.mods.BukkitOffHand;
import prelude.mods.BukkitServerTps;
import prelude.mods.BukkitTotemUsedRenderer;
import prelude.protocol.C2SPacket;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;

/**
 * The base Implementation of the ResentAPI for bukkit.
 */
public final class BaseImplementation implements Listener {
    private final PreludePlugin plugin;
    private final TpsTimer timer;

    public BaseImplementation(PreludePlugin plugin) {
        this.plugin = plugin;
        this.timer = new TpsTimer();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, timer, 1000, 50);

        Runnable tpsRunnable = () -> {
            Optional<BukkitServerTps> tpsMod = Prelude.getInstance().getMod(BukkitServerTps.class);
            if (!tpsMod.isPresent() || !tpsMod.get().isAllowed() || !tpsMod.get().isOfficiallyHooked()) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                try {
                    tpsMod.get().sendServerTpsUpdate(BukkitPlayerAdapter.adapt(plugin.getAdapter(), player), timer.getAverageTPS());
                } catch (IOException e) {
                    plugin.debug("Failed to send TPS update to " + player.getName());
                    plugin.debug(e.toString());
                }
            }
        };

        Prelude.getInstance().getMod(BukkitServerTps.class).ifPresent((tpsMod) -> {
            if (tpsMod.isOfficiallyHooked()) {
                plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, tpsRunnable, 1L, 20L);
            }
        });

        // Version Adapted Features
        VersionAdapter adapter = plugin.getAdapter();

        // Offhand mod
        if (adapter.hasOffHandSupport()) {
            Prelude.getInstance().getMod(BukkitOffHand.class).ifPresent((offHandMod) -> {
                if (!offHandMod.isOfficiallyHooked()) {
                    return;
                }
                adapter.registerOffhandListeners(offHandMod);
            });
        }

        // Totem mod
        if (adapter.hasTotemSupport()) {
            Prelude.getInstance().getMod(BukkitTotemUsedRenderer.class).ifPresent((totemMod) -> {
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

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BukkitPlayerAdapter.remove(player);
    }

    public static class ResentClientMessageListener implements PluginMessageListener {
        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] message) {

            Optional<C2SPacket> pkt;
            try {
                pkt = C2SPacket.parsePacket(message);
            } catch (Exception e) {
                PreludePlugin.getInstance().debug("Failed to parse C2SPacket!");
                PreludePlugin.getInstance().debug(e.toString());
                return;
            }

            if (!pkt.isPresent()) {
                PreludePlugin.getInstance().debug("Received message did not correspond to any packet!");
                return;
            }

            BukkitC2SPacketHandler.bindPlayer(player);
            pkt.get().processSelf(Prelude.getC2SPacketHandler());
        }
    }

    // --------------- BEGIN COPYING FROM ESSENTIALS ----------------

    /*
    * Note: I could not find an appropriate GPL 3 header copyright, so there isn't one for this piece of copied code
    * */

    /*
    * Copied from https://github.com/essentials/Essentials/blob/a2c43d822c66e617a84df9a8f074b9c3a3e32fae/Essentials/src/com/earth2me/essentials/EssentialsTimer.java
    * */
    private static class TpsTimer implements Runnable {
        private transient long lastPoll = System.nanoTime();
        private final LinkedList<Double> history = new LinkedList<>();

        public TpsTimer() {
            history.add(20d);
        }

        @Override
        public void run() {
            final long startTime = System.nanoTime();
            long timeSpent = (startTime - lastPoll) / 1000;
            if (timeSpent == 0)
                timeSpent = 1;
            if (history.size() > 10)
                history.remove();
            long tickInterval = 50;
            double tps = tickInterval * 1000000.0 / timeSpent;
            if (tps <= 21)
                history.add(tps);
            lastPoll = startTime;
        }

        public double getAverageTPS() {
            double avg = 0;
            for (Double f : history)
                if (f != null)
                    avg += f;
            return avg / history.size();
        }
    }

    // --------------- END COPYING FROM ESSENTIALS ----------------
}
