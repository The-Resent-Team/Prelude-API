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

import com.resentclient.prelude.adapter.DefaultVersionAdapter;
import com.resentclient.prelude.mods.BukkitFreeLook;
import com.resentclient.prelude.mods.BukkitOffHand;
import com.resentclient.prelude.mods.BukkitServerTps;
import com.resentclient.prelude.mods.BukkitTotemUsedRenderer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.TestOnly;
import com.resentclient.prelude.adapter.VersionAdapter;
import com.resentclient.prelude.adapter.impl.Adapter_1_11;
import com.resentclient.prelude.adapter.impl.Adapter_1_16_5;
import com.resentclient.prelude.adapter.impl.Adapter_1_17;
import com.resentclient.prelude.adapter.impl.Adapter_1_9;
import com.resentclient.prelude.api.Prelude;

import java.io.File;

@NoArgsConstructor
public final class PreludePlugin extends JavaPlugin {
    @Getter
    private static PreludePlugin instance;
    @Getter
    private VersionAdapter adapter = null;

    @TestOnly
    @SuppressWarnings("unused")
    private PreludePlugin(JavaPluginLoader loader, PluginDescriptionFile description,
                          File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onLoad() {
        VersionUtil.BukkitVersion version = VersionUtil.getServerBukkitVersion();
        if (version.isUnknown()) {
            adapter = new DefaultVersionAdapter();
            getLogger().warning("Server is running an outdated version ({}) and does not fully support all features."
                    .replace("{}", version.toString()));
        }

        else if (version.equals(VersionUtil.v1_8_8_R01)) {
            adapter = new DefaultVersionAdapter();
            getLogger().warning("Server is running an outdated version ({}) and does not fully support all features."
                    .replace("{}", version.toString()));
        }

        else if (version.isHigherThanOrEqualTo(VersionUtil.v1_9_R01) && version.isLowerThan(VersionUtil.v1_11_R01)) {
            adapter = new Adapter_1_9(this);
            getLogger().warning("Server is running an outdated version ({}) and does not fully support all features."
                    .replace("{}", version.toString()));
        }

        else if (version.isHigherThanOrEqualTo(VersionUtil.v1_11_R01) && version.isLowerThan(VersionUtil.v1_16_1_R01)) {
            adapter = new Adapter_1_11(this);
            getLogger().warning("Server is running an outdated version ({}) and does not fully support all features."
                    .replace("{}", version.toString()));
        }

        else if (version.isHigherThanOrEqualTo(VersionUtil.v1_16_1_R01) && version.isLowerThan(VersionUtil.v1_17_R01)) {
            adapter = new Adapter_1_16_5(this);
        }

        else if (version.isHigherThanOrEqualTo(VersionUtil.v1_17_R01)) {
            adapter = new Adapter_1_17(this);
        }

        else {
            adapter = new DefaultVersionAdapter();

            getLogger().warning("Server is running an outdated version ({}) and does not fully support all features."
                    .replace("{}", version.toString()));
        }

        adapter.initializeBukkitPluginMessageSender(this.getLogger());
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();

        new BukkitPrelude();

        if (getConfig().getBoolean("purely-api")) {
            getLogger().info("Partially Started Resent Client's Prelude API");
            return;
        }

        new BukkitOffHand();
        new BukkitTotemUsedRenderer();
        new BukkitFreeLook();
        new BukkitServerTps();

        getServer().getPluginManager().registerEvents(new BaseImplementation(this), this);
      
        getServer().getMessenger().registerOutgoingPluginChannel(this, Prelude.CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(
                this,
                Prelude.CHANNEL,
                new BaseImplementation.ResentClientMessageListener(this, adapter));

        getLogger().info("Fully Started Resent Client's Prelude API");
    }

    @Override
    public void onDisable() {
        getLogger().info("Stopped Prelude API");
    }

    public void debug(String message) {
        if (getConfig().getBoolean("debug")) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public ConfigurationSection getModConfig() {
        return getConfig().getConfigurationSection("mods");
    }
}