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

package com.resentclient.prelude.api;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.jetbrains.annotations.ApiStatus;
import com.resentclient.prelude.protocol.PreludeC2SPacketHandler;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public abstract class Prelude {
    public static final String CHANNEL = "resent:prelude";
    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 0;
    public static final int PATCH_VERSION = 0;

    /**
     * The API instance.
     */
    private static Prelude instance = null;

    /**
     * The PacketManager instance.
     */
    private static PreludeC2SPacketHandler c2SPacketHandler = null;

    /**
     * Get a prelude player from a UUID, the player who owns the UUID must be online
     * @param uuid uuid of the player to get
     * @return an PreludePlayer object
     * @throws IllegalStateException if the player is not online
     * @deprecated in favour of {@link #getPreludePlayer(UUID)}
     * @implSpec THIS METHOD IS MARKED FOR REMOVAL
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public abstract PreludePlayer getActor(UUID uuid) throws IllegalStateException;

    /**
     * Get a prelude player from a UUID, the player who owns the UUID must be online
     * @param uuid uuid of the player to get
     * @return a PreludePlayer object
     * @throws IllegalStateException if the player is not online
     */
    public abstract PreludePlayer getPreludePlayer(UUID uuid) throws IllegalStateException;

    /**
     * Run checks on all mods to either send the disable or the init packet to the client
     * @param preludePlayer player to validate
     */
    public abstract void validateConnection(PreludePlayer preludePlayer) throws IOException;

    /**
     * Get a ResentMod instance.
     * @param modClass the mod to get
     * @return the instance of the mod
     * @throws IllegalArgumentException if the modClass is not final or is abstract
     */
    public abstract <T extends ResentMod> Optional<T> getMod(Class<T> modClass) throws IllegalArgumentException;

    /**
     * Register a ResentMod hook.
     * @param mod the mod instance to register
     * @apiNote Internal use only
     */
    public abstract void addMod(ResentMod mod);

    /**
     * Get a set of all registered mods.
     * @return immutable set of all registered mods
     * @since 2.0.1
     */
    public abstract Set<ResentMod> getMods();

    /**
     * Get the API instance.
     * @return the instance of the api
     */
    @Immutable
    public static Prelude getInstance() {
        return instance;
    }

    /**
     * Get the PacketManager instance.
     * @return the instance of the packet manager
     */
    @Immutable
    @ApiStatus.Internal
    public static PreludeC2SPacketHandler getC2SPacketHandler() {
        return c2SPacketHandler;
    }

    /**
     * Set the instance.
     * @throws IllegalStateException if the instance is already assigned
     */
    @ApiStatus.Internal
    public static void setInstance(Prelude newInstance) {
        if (instance != null) {
            throw new IllegalStateException("Instance has already been set");
        }
        instance = newInstance;
    }

    /**
     * Set the instance of the packet manager.
     * @throws IllegalStateException if the packet manager instance is already assigned
     */
    @ApiStatus.Internal
    public static void setC2SPacketHandler(PreludeC2SPacketHandler newPacketHandler) {
        if (c2SPacketHandler != null) {
            throw new IllegalStateException("Packet Manager instance has already been set");
        }
        c2SPacketHandler = newPacketHandler;
    }
}