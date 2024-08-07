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

import lombok.Getter;
import com.resentclient.prelude.protocol.PreludeS2CPacket;
import com.resentclient.prelude.protocol.packets.c2s.ClientHandshakePreludeC2SPacket;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.UUID;

@Getter
@SuppressWarnings("unused")
public abstract class PreludePlayer {
    private final String username;
    private final UUID uuid;
    private final Info info;

    public PreludePlayer(String username, UUID uuid, Info info) {
        this.username = username;
        this.uuid = uuid;
        this.info = info;
    }

    /**
     * Sends the packet
     * @param packet S2CPacket to send
     * @author cire3
     * @since 1.0.0
     */
    public void sendPacket(PreludeS2CPacket packet) throws IOException {
        sendBytes(packet.toBytes());
    }

    /**
     * Sends raw binary data
     * @param bytes bytes to send
     * @author cire3
     * @since 1.0.0
     */
    @ApiStatus.Internal
    public abstract void sendBytes(byte[] bytes) throws IOException;

    public static class Info {
        public static Info UNKNOWN_INFO = new Info(null, -1, -1, -1, null, false, null);

        public final String username;
        public final int resentMajorVersion;
        public final int resentMinorVersion;
        public final int resentBuildInteger;
        public final ClientHandshakePreludeC2SPacket.ClientType clientType;
        public final boolean clientClaimsSelfIsRankedPlayer;
        public final String[] enabledMods;

        public Info(String username, int resentMajorVersion, int resentMinorVersion, int resentBuildInteger, ClientHandshakePreludeC2SPacket.ClientType clientType, boolean clientClaimsSelfIsRankedPlayer, String[] enabledMods) {
            this.username = username;
            this.resentMajorVersion = resentMajorVersion;
            this.resentMinorVersion = resentMinorVersion;
            this.resentBuildInteger = resentBuildInteger;
            this.clientType = clientType;
            this.clientClaimsSelfIsRankedPlayer = clientClaimsSelfIsRankedPlayer;
            this.enabledMods = enabledMods;
        }
    }
}
