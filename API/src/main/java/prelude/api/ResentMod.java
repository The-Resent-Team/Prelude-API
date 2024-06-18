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
