package prelude.adapter.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import prelude.adapter.BukkitPlayerAdapter;
import prelude.adapter.VersionAdapter;
import prelude.api.Prelude;
import prelude.api.mods.AnchorRenderer;
import prelude.api.mods.OffHand;
import prelude.api.mods.TotemUsedRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        plugin.getServer().getPluginManager().registerEvents(new TotemListeners(), plugin);
    }

    @Override
    public Runnable getOffHandRunnable(OffHand offhandMod) {
        // This is my solution for monitoring offhand items
        // Cires was to track the ItemStacks client side, while this makes more sense,
        // I think we should take as much stress off the client as possible
        // if you come up with a better implementation please make a PR
        return () -> {
            if (!offhandMod.isAllowed() || !offhandMod.isOfficiallyHooked()) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack currentOffhand = player.getInventory().getItemInOffHand();
                if (!offhandItemMap.containsKey(player)) {
                    offhandItemMap.put(player, currentOffhand);
                    continue;
                }
                ItemStack cachedOffhand = offhandItemMap.get(player);
                if (cachedOffhand == currentOffhand) {
                    continue;
                }
                offhandItemMap.replace(player, currentOffhand);
                if (currentOffhand.getType() == Material.AIR) {
                    offhandMod.sendOffhandUnEquipEvent(BukkitPlayerAdapter.getPreludePlayer(plugin, player),
                            Material.AIR.name(), false);
                } else {
                    offhandMod.sendOffhandEquipEvent(BukkitPlayerAdapter.getPreludePlayer(plugin, player),
                            currentOffhand.getType().name(), !currentOffhand.getEnchantments().isEmpty());
                }
            }
        };
    }

    public class TotemListeners implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onResurrectEvent(EntityResurrectEvent event) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                Optional<TotemUsedRenderer> mod = Prelude.getInstance().getMod(TotemUsedRenderer.class);
                if (!mod.isPresent() || !mod.get().isAllowed() || !mod.get().isOfficiallyHooked()) {
                    return;
                }
                mod.get().sendTotemPoppedEvent(BukkitPlayerAdapter.getPreludePlayer(plugin, player));
            }
        }
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
