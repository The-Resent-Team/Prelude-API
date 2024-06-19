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
import prelude.api.Prelude;
import prelude.api.mods.AnchorRenderer;
import prelude.api.mods.OffHand;
import prelude.api.mods.TotemUsedRenderer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

public final class Adapter_1_16_5 implements VersionAdapter {
    private final JavaPlugin plugin;
    private final Map<Player, ItemStack> offhandItemMap = new HashMap<>();

    public Adapter_1_16_5(JavaPlugin plugin) {
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

    public class TotemListeners implements Listener {
        TotemUsedRenderer totemMod;

        public TotemListeners(TotemUsedRenderer totemMod) {
            this.totemMod = totemMod;
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onResurrectEvent(EntityResurrectEvent event) throws IOException {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                totemMod.sendTotemPoppedEvent(BukkitPlayerAdapter.getPreludePlayer(plugin, player));
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

            anchorMod.sendPlacedAnchorPacket(BukkitPlayerAdapter.getPreludePlayer(plugin, event.getPlayer()), x, y, z);
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
                anchorMod.sendBlownUpAnchorPacket(BukkitPlayerAdapter.getPreludePlayer(plugin, event.getPlayer()),
                        x, y, z);
                return;
            }

            if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GLOWSTONE) {
                anchorMod.sendInteractedAnchorPacket(BukkitPlayerAdapter.getPreludePlayer(plugin, event.getPlayer()),
                        x, y, z, charges + 1);
            } else if (charges != 0) {
                // it is charged, and they didnt interact with a glowstone block
                // send blown up packet
                anchorMod.sendBlownUpAnchorPacket(BukkitPlayerAdapter.getPreludePlayer(plugin, event.getPlayer()),
                        x, y, z);
            }
        }
    }

    public class OffhandListeners implements Listener {
        OffHand offHand;
        private Map<Player, ItemStack> playerToOffhand = new HashMap<>();

        public OffhandListeners(OffHand offHand) {
            this.offHand = offHand;
        }

        /*
         * This event is actually artificially fired by prelude when it receives
         * an
         * */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onOffhandSwapViaKeybind(PlayerSwapHandItemsEvent event) throws IOException {
            offHand.sendOffhandEvent(BukkitPlayerAdapter.getPreludePlayer(plugin, event.getPlayer()),
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
                        offHand.sendOffhandEvent(BukkitPlayerAdapter.getPreludePlayer(plugin, player),
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
}
