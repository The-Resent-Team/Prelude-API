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
import com.resentclient.prelude.api.PreludePlayer;
import com.resentclient.prelude.api.Prelude;
import com.resentclient.prelude.api.mods.TotemUsedRenderer;

import java.io.IOException;

public final class BukkitTotemUsedRenderer extends TotemUsedRenderer {

    public BukkitTotemUsedRenderer() {
        super();
        Prelude.getInstance().addMod(this);

        enabled = true;
    }

    @Override
    public boolean isAllowed() {
        return PreludePlugin.getInstance().getModConfig().getBoolean("totem-tweaks.allowed", true);
    }

    @Override
    public boolean isOfficiallyHooked() {
        return true;
    }

    @Override
    public void sendTotemPoppedEvent(PreludePlayer preludePlayer) throws IOException {
        super.sendTotemPoppedEvent(preludePlayer);
        PreludePlugin.getInstance().debug("Dispatched TotemPoppedEvent to " + preludePlayer);
    }
}
