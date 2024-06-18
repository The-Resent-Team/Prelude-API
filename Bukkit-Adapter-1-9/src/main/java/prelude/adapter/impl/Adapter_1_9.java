package prelude.adapter.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

public final class Adapter_1_9 implements VersionAdapter {
    private final JavaPlugin plugin;

    public Adapter_1_9(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerAnchorListener(AnchorRenderer anchorMod) {
        // Do nothing
    }

    @Override
    public void registerTotemListener(TotemUsedRenderer totemMod) {
        // Do nothing
    }

    @Override
    public void registerOffhandItemSwapListeners(OffHand offHandMod) {
        plugin.getServer().getPluginManager().registerEvents(new OffhandListeners(offHandMod), plugin);
    }

    @Override
    public boolean hasOffHandSupport() {
        return true;
    }

    public class OffhandListeners implements Listener {
        OffHand offHand;

        public OffhandListeners(OffHand offHand) {
            this.offHand = offHand;
        }

        /*
        * This event is actually artificially fired by prelude when it receives
        * an
        * */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onOffhandSwapViaKeybind(PlayerSwapHandItemsEvent event) throws IOException {
            offHand.sendOffhandEvent(BukkitPlayerAdapter.getPreludePlayer(plugin, event.getPlayer()), event.getOffHandItem().toString(), true);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onInventoryDrag(InventoryDragEvent event) {

        }
    }
}
