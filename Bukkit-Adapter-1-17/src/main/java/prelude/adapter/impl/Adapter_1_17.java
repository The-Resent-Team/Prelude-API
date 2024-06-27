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

package prelude.adapter.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import prelude.adapter.BukkitPlayerAdapter;
import prelude.adapter.VersionAdapter;
import prelude.api.mods.AnchorRenderer;
import prelude.api.mods.OffHand;
import prelude.api.mods.TotemUsedRenderer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Adapter_1_17 implements VersionAdapter {
    private final JavaPlugin plugin;

    public Adapter_1_17(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerAnchorListener(AnchorRenderer anchorMod) {
        plugin.getServer().getPluginManager().registerEvents(new AnchorListeners(anchorMod), plugin);
    }

    @Override
    public void registerTotemListener(TotemUsedRenderer totemMod) {
        plugin.getServer().getPluginManager().registerEvents(new TotemListeners(totemMod), plugin);
    }

    @Override
    public void registerOffhandListeners(OffHand offHandMod) {
        plugin.getServer().getPluginManager().registerEvents(new OffhandListeners(offHandMod), plugin);
    }

    @Override
    public boolean equipSlotToOffhand(Player player, int slot) {
        if (player == null) return false;
        try {
            ItemStack attemptedItemToSwap = player.getOpenInventory().getItem(slot);
            ItemStack offhand = player.getInventory().getItemInOffHand();

            player.getInventory().setItemInOffHand(attemptedItemToSwap);
            player.getOpenInventory().setItem(slot, offhand);

            player.updateInventory();
        } catch (Exception e) {
            // most likely someone trying to abuse prelude into spamming console
            return false;
        }

        return true;
    }

    public class TotemListeners implements Listener {
        TotemUsedRenderer totemMod;

        public TotemListeners(TotemUsedRenderer totemMod) {
            this.totemMod = totemMod;
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onResurrectEvent(EntityResurrectEvent event) throws IOException {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                totemMod.sendTotemPoppedEvent(BukkitPlayerAdapter.adapt(Adapter_1_17.this, player));
            }
        }
    }

    public class AnchorListeners implements Listener {
        AnchorRenderer anchorMod;

        public AnchorListeners(AnchorRenderer anchorMod) {
            this.anchorMod = anchorMod;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onAnchorPlace(BlockPlaceEvent event) throws IOException {
            if (event.getBlockPlaced().getType() != Material.RESPAWN_ANCHOR) {
                return;
            }

            int x = event.getBlockPlaced().getX();
            int y = event.getBlockPlaced().getY();
            int z = event.getBlockPlaced().getZ();

            anchorMod.sendPlacedAnchorPacket(BukkitPlayerAdapter.adapt(Adapter_1_17.this, event.getPlayer()), x, y, z);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onAnchorInteract(PlayerInteractEvent event) throws IOException {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            if (event.getClickedBlock() == null
                    || event.getClickedBlock().getType() != Material.RESPAWN_ANCHOR) {
                return;
            }

            int x = event.getClickedBlock().getX();
            int y = event.getClickedBlock().getY();
            int z = event.getClickedBlock().getZ();

            int charges = ((RespawnAnchor) event.getClickedBlock().getBlockData()).getCharges();

            // detect if the anchor is fully charged
            // if anchor is fully charged, interacting at all
            // will cause explosion
            if (charges == ((RespawnAnchor) event.getClickedBlock().getBlockData()).getMaximumCharges()) {
                anchorMod.sendBlownUpAnchorPacket(BukkitPlayerAdapter.adapt(Adapter_1_17.this, event.getPlayer()),
                        x, y, z);
                return;
            }

            if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GLOWSTONE) {
                anchorMod.sendInteractedAnchorPacket(BukkitPlayerAdapter.adapt(Adapter_1_17.this, event.getPlayer()),
                        x, y, z, charges + 1);
            } else if (charges != 0) {
                // it is charged, and they didnt interact with a glowstone block
                // send blown up packet
                anchorMod.sendBlownUpAnchorPacket(BukkitPlayerAdapter.adapt(Adapter_1_17.this, event.getPlayer()),
                        x, y, z);
            }
        }
    }

    public class OffhandListeners implements Listener {
        OffHand offHand;
        private final Map<Player, ItemStack> playerToOffhand = new HashMap<>();

        public OffhandListeners(OffHand offHand) {
            this.offHand = offHand;
        }

        /*
         * This event is actually artificially fired by prelude when it receives
         * an
         * */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onOffhandSwapViaKeybind(PlayerSwapHandItemsEvent event) throws IOException {
            offHand.sendOffhandEvent(BukkitPlayerAdapter.adapt(Adapter_1_17.this, event.getPlayer()),
                    serialize(event.getOffHandItem()), true);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onInventoryDrag(InventoryDragEvent event) {
            if (!(event.getWhoClicked() instanceof Player))
                return;

            Player player = (Player) event.getWhoClicked();

            compareOffhandsNextTick(player);
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player))
                return;

            Player player = (Player) event.getWhoClicked();

            if (event.getAction() == InventoryAction.CLONE_STACK || event.getAction() == InventoryAction.NOTHING)
                return;

            compareOffhandsNextTick(player);
        }

        private void compareOffhandsNextTick(Player player) {
            playerToOffhand.put(player, player.getInventory().getItemInOffHand());

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (!Objects.equals(player.getInventory().getItemInOffHand(), playerToOffhand.get(player))) {
                    try {
                        offHand.sendOffhandEvent(BukkitPlayerAdapter.adapt(Adapter_1_17.this, player),
                                serialize(player.getInventory().getItemInOffHand()), false);
                    } catch (IOException e) {
                        // this shouldn't actually be thrown, this is for safety purposes
                        plugin.getLogger().log(Level.SEVERE, e.toString(), e);
                    }
                }

                playerToOffhand.remove(player);
            }, 1);
        }
    }

    public static String serialize(ItemStack itemStack) {
        return itemStack == null ? "ItemStack{NULL}" : itemStack.toString();
    }

    @Override
    public boolean hasAnchorSupport() {
        return true;
    }

    @Override
    public boolean hasTotemSupport() {
        return true;
    }

    @Override
    public boolean hasOffHandSupport() {
        return true;
    }

    @Override
    public BukkitPluginMessageSender_1_17 getMessageSender() {
        return BukkitPluginMessageSender_1_17.getInstance();
    }

    @Override
    public void initializeBukkitPluginMessageSender(Logger logger) {
        new BukkitPluginMessageSender_1_17(logger);
    }
}
