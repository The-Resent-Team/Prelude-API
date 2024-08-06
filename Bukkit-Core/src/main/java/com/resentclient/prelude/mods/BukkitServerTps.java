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

package com.resentclient.prelude.mods;

import com.resentclient.prelude.PreludePlugin;
import com.resentclient.prelude.adapter.BukkitPlayerAdapter;
import com.resentclient.prelude.api.PreludePlayer;
import com.resentclient.prelude.api.Prelude;
import com.resentclient.prelude.api.mods.ServerTps;

import java.io.IOException;

public final class BukkitServerTps extends ServerTps {

    public BukkitServerTps() {
        super();
        Prelude.getInstance().addMod(this);
        enabled = true;
    }

    @Override
    public void sendServerTpsUpdate(PreludePlayer preludePlayer, double currentTps) throws IOException {
        super.sendServerTpsUpdate(preludePlayer, currentTps);
        if (!BukkitPlayerAdapter.NON_RESENT_CLIENT_PLAYER.equals(preludePlayer))
            PreludePlugin.getInstance().debug("Dispatched ServerTpsUpdate to " + preludePlayer.getUsername());
    }

    @Override
    public boolean isAllowed() {
        return PreludePlugin.getInstance().getModConfig().getBoolean("server-tps.allowed", true);
    }

    @Override
    public boolean isOfficiallyHooked() {
        return true;
    }
}
