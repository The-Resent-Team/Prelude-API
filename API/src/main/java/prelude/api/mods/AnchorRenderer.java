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

package prelude.api.mods;

import prelude.api.PreludePlayer;
import prelude.api.ResentMod;
import prelude.protocol.packets.s2c.RespawnAnchorUpdatePacket;

import java.io.IOException;

public abstract class AnchorRenderer extends ResentMod {
    protected AnchorRenderer() {
        super();
    }

    public void sendPlacedAnchorPacket(PreludePlayer preludePlayer, int x, int y, int z) throws IOException {
        sendInteractedAnchorPacket(preludePlayer, x, y, z, 1);
    }

    /**
     * Sends a packet with {@code charge}
     * Sent charge -
     * 0 - Exploded
     * 1 - Empty
     * 2 - Charge 1
     * 3 - Charge 2
     * 4 - Charge 3
     * 5 - Charge 4
     * @param charge 1 to 4, describing the amount of glowstone in the anchor
     */
    public void sendInteractedAnchorPacket(PreludePlayer preludePlayer, int x, int y, int z, int charge) throws IOException {
        preludePlayer.sendPacket(
                RespawnAnchorUpdatePacket.builder()
                        .x(x)
                        .y(y)
                        .z(z)
                        .charge(charge + 1)
                        .build()
        );
    }

    public void sendBlownUpAnchorPacket(PreludePlayer preludePlayer, int x, int y, int z) throws IOException {
        sendInteractedAnchorPacket(preludePlayer, x, y, z, 0);
    }

    @Override
    public final String getModId() {
        return "respawn_anchor_renderer";
    }
}
