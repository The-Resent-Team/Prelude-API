package prelude.adapter;

import org.bukkit.entity.Player;
import prelude.api.PreludePlayer;
import prelude.api.mods.AnchorRenderer;
import prelude.api.mods.OffHand;
import prelude.api.mods.TotemUsedRenderer;

import java.util.logging.Logger;


public interface VersionAdapter {

    /**
     * Register the anchor mod listeners
     * @param anchorMod the instance of the mod to use
     */
    void registerAnchorListener(AnchorRenderer anchorMod);

    /**
     * Register the totem tweaks mod listeners
     * @param totemMod the instance of the mod to use
     */
    void registerTotemListener(TotemUsedRenderer totemMod);

    /**
     * Register the offhand mod listeners
     * @param offHandMod the instance of the mod to use
     */
    void registerOffhandListeners(OffHand offHandMod);

    /**
     * Check whether the adapter supports anchor renderer mod.
     * @return true if it does
     * @apiNote Anchors are in MC Version 1.16+
     */
    default boolean hasAnchorSupport() {
        return false;
    }

    /**
     * Check whether the adapter supports totem tweaks mod.
     * @return true if it does
     * @apiNote Totems are in MC Version 1.11+
     */
    default boolean hasTotemSupport() {
        return false;
    }

    /**
     * Check whether the adapter supports offhand mod.
     * @return true if it does
     * @apiNote OffHand is in MC Version 1.9+
     */
    default boolean hasOffHandSupport() {
        return false;
    }

    /**
     * Attempts to put the item in specified slot into the offhand, and the offhand into specified slot
     * @return true if it succeeds
     * @apiNote this will call an PlayerSwapHandItemsEvent
     */
    default boolean equipSlotToOffhand(Player player, int slot) {
        return false;
    }

    /**
     * Sends messages in a channel with all the potion effect ids to prevent the bug
     */
    default void sendPotionEffects() {

    }

    /**
     * Returns the plugin message sender for this adapter
     * @return plugin message sender
     */
    default AbstractBukkitPluginMessageSender getMessageSender() {
        return BukkitPluginMessageSender.getInstance();
    }

    /**
     * Initializes the BukkitPluginMessageSender for this adapter
     */
    default void initializeBukkitPluginMessageSender(Logger logger) {
        new BukkitPluginMessageSender(logger);
    }
}
