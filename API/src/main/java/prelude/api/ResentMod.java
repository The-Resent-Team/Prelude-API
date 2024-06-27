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

package prelude.api;

import lombok.Getter;
import prelude.protocol.packets.s2c.ModStatusPacket;

import java.io.IOException;

@Getter
public abstract class ResentMod {
    protected boolean enabled = false;

    protected ResentMod() {
    }

    public void initMod(PreludePlayer preludePlayer) throws IOException {
        preludePlayer.sendPacket(ModStatusPacket.builder()
                .modStatus(ModStatusPacket.ModStatus.SUPPORTED)
                .modIdentifier(getModId())
                .build());
    }

    public void disableMod(PreludePlayer preludePlayer) throws IOException {
        preludePlayer.sendPacket(ModStatusPacket.builder()
                .modStatus(ModStatusPacket.ModStatus.DISABLE)
                .modIdentifier(getModId())
                .build());
    }

    public abstract String getModId();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean isAllowed();

    public boolean isOfficiallyHooked() {
        return false;
    }
}
