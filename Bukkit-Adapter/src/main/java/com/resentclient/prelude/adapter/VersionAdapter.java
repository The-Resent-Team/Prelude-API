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
 *
 */

package com.resentclient.prelude.adapter;

import com.resentclient.prelude.protocol.packets.s2c.play.UpdateOffhandPreludeS2CPacket;
import org.bukkit.entity.Player;
import com.resentclient.prelude.api.mods.OffHand;
import com.resentclient.prelude.api.mods.TotemUsedRenderer;

import java.io.IOException;
import java.util.logging.Logger;

public interface VersionAdapter {
    /**
     * Register the totem tweaks mod listeners
     * @param totemMod the instance of the mod to use
     */
    default void registerTotemListener(TotemUsedRenderer totemMod) {

    }

    /**
     * Register the offhand mod listeners
     * @param offHandMod the instance of the mod to use
     */
    default void registerOffhandListeners(OffHand offHandMod) {

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
     * @apiNote this will call an PlayerSwapHandItemsEvent
     */
    default void equipSlotToOffhand(Player player, int slot) {
        try {
            BukkitPlayerAdapter.adapt(this, player).sendPacket(
                    UpdateOffhandPreludeS2CPacket.builder()
                            .serializedItem("ItemStack{NULL}")
                            .canClientDisregardThis(false)
                            .build());
        } catch (IOException e) {
            // ???????????????????????????????
        }
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
