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

package com.resentclient.prelude;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.resentclient.prelude.adapter.BukkitPlayerAdapter;
import com.resentclient.prelude.api.Prelude;
import com.resentclient.prelude.api.PreludePlayer;
import com.resentclient.prelude.api.ResentMod;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class BukkitPrelude extends Prelude {

    private static final Set<ResentMod> mods = new HashSet<>();

    public BukkitPrelude() {
        setInstance(this);
        setC2SPacketHandler(new BukkitC2SPacketHandler());
    }

    @Override
    public PreludePlayer getActor(UUID uuid) throws IllegalStateException {
        return getPreludePlayer(uuid);
    }

    @Override
    public PreludePlayer getPreludePlayer(UUID uuid) throws IllegalStateException {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            throw new IllegalStateException("An actor must be online! Attempted UUID: " + uuid.toString());
        }
        return BukkitPlayerAdapter.adapt(PreludePlugin.getInstance().getAdapter(), player);
    }

    @Override
    public void validateConnection(PreludePlayer preludePlayer) throws IOException {
//        TODO
//        ServerHandshakePacket pkt = ServerHandshakePacket.builder().majorVersion().build()

        PreludePlugin.getInstance().debug("Validating mods for " + preludePlayer);
        for (ResentMod mod : mods) {
            if (!mod.isEnabled()) {
                PreludePlugin.getInstance().debug(String.format("Mod %s did not get enabled",
                        mod.getClass().getSimpleName()));
                continue;
            }
            if (!mod.isAllowed()) {
                mod.disableMod(preludePlayer);
                PreludePlugin.getInstance().debug(String.format("Mod %s is not allowed and was disabled for %s",
                        mod.getClass().getSimpleName(), preludePlayer.getUsername()));
                continue;
            }
            if (mod.isOfficiallyHooked()) {
                mod.initMod(preludePlayer);
            }
        }

        // TODO
//        if (PreludePlugin.getInstance().getConfig().getBoolean("patches.potion-effect-kick-patch.enabled", true)) {
//            if (player != null) {
//                for (PotionEffect effect : player.getActivePotionEffects()) {
//
//                }
//            }
//        }
    }

    @Override
    public <T extends ResentMod> Optional<T> getMod(Class<T> modClass) {
        Preconditions.checkArgument(Modifier.isFinal(modClass.getModifiers()));
        Preconditions.checkArgument(!Modifier.isAbstract(modClass.getModifiers()));

        return mods.stream()
                .filter(mod -> modClass.isAssignableFrom(mod.getClass()))
                .map(modClass::cast)
                .findFirst();
    }

    @Override
    public void addMod(ResentMod mod) {
        mods.add(mod);
    }

    @Override
    public Set<ResentMod> getMods() {
        return ImmutableSet.copyOf(mods);
    }
}
