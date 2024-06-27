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

package prelude.mods;

import prelude.PreludePlugin;
import prelude.api.PreludePlayer;
import prelude.api.Prelude;
import prelude.api.mods.AnchorRenderer;

import java.io.IOException;

public final class BukkitAnchorRenderer extends AnchorRenderer {
    public BukkitAnchorRenderer() {
        super();
        Prelude.getInstance().addMod(this);

        enabled = true;
    }

    @Override
    public boolean isAllowed() {
        return PreludePlugin.getInstance().getModConfig().getBoolean("anchor-renderer.allowed", true);
    }

    @Override
    public boolean isOfficiallyHooked() {
        return true;
    }

    @Override
    public void sendPlacedAnchorPacket(PreludePlayer preludePlayer, int x, int y, int z) throws IOException {
        super.sendPlacedAnchorPacket(preludePlayer, x, y, z);
        PreludePlugin.getInstance().debug("Dispatched PlacedAnchorEvent to " + preludePlayer);
    }

    @Override
    public void sendInteractedAnchorPacket(PreludePlayer preludePlayer, int x, int y, int z, int charge) throws IOException {
        super.sendInteractedAnchorPacket(preludePlayer, x, y, z, charge);
        PreludePlugin.getInstance().debug("Dispatched InteractedAnchorEvent to " + preludePlayer);
    }
}
