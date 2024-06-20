package prelude.adapter.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
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

public final class Adapter_1_11 implements VersionAdapter {
    private final JavaPlugin plugin;
    private final Map<Player, ItemStack> offhandItemMap = new HashMap<>();

    public Adapter_1_11(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerAnchorListener(AnchorRenderer anchorMod) {
        // Do nothing
    }

    @Override
    public void registerTotemListener(TotemUsedRenderer totemMod) {
        plugin.getServer().getPluginManager().registerEvents(new TotemListeners(totemMod), plugin);
    }

    @Override
    public void registerOffhandListeners(OffHand offHand) {
        plugin.getServer().getPluginManager().registerEvents(new OffhandListeners(offHand), plugin);
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
                totemMod.sendTotemPoppedEvent(BukkitPlayerAdapter.adapt(Adapter_1_11.this, player));
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
            offHand.sendOffhandEvent(BukkitPlayerAdapter.adapt(Adapter_1_11.this, event.getPlayer()),
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
                        offHand.sendOffhandEvent(BukkitPlayerAdapter.adapt(Adapter_1_11.this, player),
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
    public boolean hasTotemSupport() {
        return true;
    }

    @Override
    public boolean hasOffHandSupport() {
        return true;
    }
}
