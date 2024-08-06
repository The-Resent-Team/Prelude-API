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

import com.resentclient.prelude.api.PreludePlayer;
import com.resentclient.prelude.bukkit.mods.BukkitOffHand;
import com.resentclient.prelude.bukkit.mods.BukkitServerTps;
import com.resentclient.prelude.bukkit.mods.BukkitTotemUsedRenderer;
import com.resentclient.resentxprelude.AlgorithmRSA;
import com.resentclient.resentxprelude.SHA256Digest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import com.resentclient.prelude.adapter.BukkitPlayerAdapter;
import com.resentclient.prelude.adapter.VersionAdapter;
import com.resentclient.prelude.api.Prelude;
import com.resentclient.prelude.protocol.PreludeC2SPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.resentclient.resentxprelude.AlgorithmRSA.PRELUDE_CLIENT_PUBLIC_E;

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
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BukkitPlayerAdapter.remove(player);
    }

    public static class ResentClientMessageListener implements PluginMessageListener {
        public final JavaPlugin plugin;
        public final VersionAdapter adapter;

        private static final Map<Integer, BigInteger> N_CACHE = new HashMap<>();
        private static final Set<Integer> FAILED_N = new HashSet<>();
        private static final byte[] TEMP_BUFFER = new byte[32];
        private static final byte[] RES_PRE_VER_ARR = new byte[] { 'R', 'E', 'S', 'P', 'R', 'E', 'V', 'E', 'R' };

        public ResentClientMessageListener(JavaPlugin plugin, VersionAdapter adapter) {
            this.plugin = plugin;
            this.adapter = adapter;
        }

        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] message) {
            if (!Prelude.CHANNEL.equals(channel))
                return;

            if (message == null || message.length < 1)
                return;

            if (shouldSkipPacket(player, message))
                return;

            Optional<PreludeC2SPacket> pkt;
            try {
                pkt = PreludeC2SPacket.parsePacket(message);
            } catch (Exception e) {
                PreludePlugin.getInstance().debug("Failed to parse C2SPacket!");
                PreludePlugin.getInstance().debug(e.toString());
                return;
            }

            if (!pkt.isPresent()) {
                PreludePlugin.getInstance().debug("Received message did not correspond to any packet!");
                return;
            }

            BukkitC2SPacketHandler.bindPlayer(player); // set to active player becuz pro code design

            pkt.get().processSelf(Prelude.getC2SPacketHandler());
        }

        private boolean shouldSkipPacket(Player player, byte[] message) {
            // if they tried to hang prelude previously
            // just ignore
            if (BukkitPlayerAdapter.didPlayerTryToHang(player))
                return true;

            // a key implementation detail is that
            // we don't kick them for failing to verify
            // we just don't respond to their C2SPackets
            // they can keep on trying to verify (until another plugin/server)
            // kicks them
            if (!BukkitPlayerAdapter.isPlayerVerified(player)) {
                PreludePlayer.Info info = BukkitPlayerAdapter.adapt(adapter, player).getInfo();
                if (info == null)
                    return true; // they haven't sent info yet

                // ids for Handshake and HandshakeAcknowledge
                if (message[0] != 0 && message[0] != 1) {
                    byte[] payload = BukkitPlayerAdapter.getPlayerPayload(player);
                    final BigInteger PUBLIC_N = getNForInfo(info);

                    if (PUBLIC_N == null) {
                        // TODO: special handling for this, I can't just mark them verified so they can try to exploit prelude
                        return true;
                    }

                    // we've sent verification, check if this packet is a verification response
                    if (payload != null) {
                        ByteArrayInputStream is = new ByteArrayInputStream(message);
                        if (is.read() == 'R' && is.read() == 'E' && is.read() == 'S' &&
                                is.read() == 'P' && is.read() == 'R' && is.read() == 'E' &&
                                is.read() == 'V' && is.read() == 'E' && is.read() == 'R') {

                            byte[] sentBackPayload = new byte[payload.length];
                            try {
                                if (is.read(sentBackPayload) != sentBackPayload.length)
                                    return true;
                                if (!Arrays.equals(sentBackPayload, payload))
                                    return true;

                                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                int iterations = 0; // don't let them hang Prelude

                                // read at most 320 bytes (hash shouldn't be over 32 bytes anyway tf)
                                while (is.read(TEMP_BUFFER) != -1 && ++iterations < 10)
                                    bao.write(TEMP_BUFFER);

                                if (iterations == 10) {
                                    BukkitPlayerAdapter.markPlayerTriedToHang(player);
                                    return true; // they tried to hang prelude
                                }

                                byte[] responseHash = AlgorithmRSA.cipherToBytes(AlgorithmRSA.decrypt(new BigInteger(bao.toByteArray()),
                                        PRELUDE_CLIENT_PUBLIC_E, PUBLIC_N));
                                if (responseHash.length != 32) // sha 256 hash is 32 bytes
                                    return true;

                                SHA256Digest digest = new SHA256Digest();
                                digest.update(RES_PRE_VER_ARR, 0, 9);
                                digest.update(sentBackPayload, 0, sentBackPayload.length);
                                digest.doFinal(TEMP_BUFFER, 0);

                                if (!Arrays.equals(TEMP_BUFFER, responseHash))
                                    return true;

                                // hash checks out, we decrypted with public key successfully so client has
                                // the proper private key, sent payload matches, they didn't try to
                                BukkitPlayerAdapter.markPlayerVerified(player);
                                return false;
                            } catch (IOException e) {
                                if (plugin instanceof PreludePlugin)
                                    ((PreludePlugin)plugin).debug("Exception while reading verification response!");
                                else
                                    plugin.getLogger().warning("Exception while reading verification response!");
                                return true;
                            }
                        }
                    } else {
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        try {
                            // 25 bytes
                            bao.write("RESENTXPRELDEVERIFICATION".getBytes(StandardCharsets.US_ASCII));
                            byte[] bytes = new byte[7];
                            new Random().nextBytes(bytes);
                            bao.write(bytes);

                            bytes = bao.toByteArray();
                            byte[] encryptedMessage = AlgorithmRSA.cipherToBytes(AlgorithmRSA.encrypt(AlgorithmRSA.bytesToCipher(bytes),
                                    PRELUDE_CLIENT_PUBLIC_E, PUBLIC_N));

                            BukkitPlayerAdapter.adapt(adapter, player).sendBytes(encryptedMessage);
                            BukkitPlayerAdapter.markSentPlayerVerification(player, bytes);
                        } catch (IOException e) {
                            if (plugin instanceof PreludePlugin)
                                ((PreludePlugin)plugin).debug("Failed to send verification payload!");
                            else
                                plugin.getLogger().warning("Failed to send verification payload!");
                            return false; // we failed to send, don't disable prelude if this happens
                        }
                    }

                    return true; // it isnt handshake/acknowledge, and they arent verified, skip parsing packet
                }
                // it is a handshake/handshake acknowledge, we can let them through
                return false;
            }

            return false; // they are verified
        }

        private BigInteger getNForInfo(PreludePlayer.Info info) {
            if (FAILED_N.contains(info.resentBuildInteger))
                return null;

            BigInteger cached = N_CACHE.get(info.resentBuildInteger);
            if (cached != null)
                return cached;

            try {
                Field N = AlgorithmRSA.class.getDeclaredField("PRELUDE_CLIENT_PUBLIC_N_%maj%_%min%"
                        .replace("%maj%", info.resentMajorVersion + "")
                        .replace("%min%", info.resentMinorVersion + ""));
                BigInteger value = (BigInteger) N.get(info);
                if (value == null)
                    FAILED_N.add(info.resentBuildInteger);

                N_CACHE.put(info.resentBuildInteger, value);

                return value;
            } catch (Exception e) {
                return null;
            }
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
