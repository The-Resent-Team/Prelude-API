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
import prelude.protocol.packets.s2c.UpdateOffhandPacket;

import java.io.IOException;

public abstract class OffHand extends ResentMod {
    protected OffHand() {
        super();
    }

    public void sendOffhandEvent(PreludePlayer preludePlayer, String serializedItem, boolean canClientIgnore) throws IOException {
        preludePlayer.sendPacket(
                UpdateOffhandPacket.builder()
                        .serializedItem(serializedItem)
                        .canClientDisregardThis(canClientIgnore)
                        .build()
        );
    }

    @Override
    public final String getModId() {
        return "offhand_renderer";
    }
}
