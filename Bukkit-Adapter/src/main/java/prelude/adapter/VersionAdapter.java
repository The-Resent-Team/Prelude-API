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

package prelude.adapter;

import org.bukkit.entity.Player;
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
