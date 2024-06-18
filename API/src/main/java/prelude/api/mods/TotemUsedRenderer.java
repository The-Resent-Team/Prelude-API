package prelude.api.mods;

import prelude.api.PreludePlayer;
import prelude.api.ResentMod;
import prelude.protocol.packets.s2c.TotemUsedPacket;

import java.io.IOException;

public abstract class TotemUsedRenderer extends ResentMod {
    protected TotemUsedRenderer() {
        super();
    }

    public void sendTotemPoppedEvent(PreludePlayer preludePlayer) throws IOException {
        preludePlayer.sendPacket(new TotemUsedPacket());
    }

    @Override
    public String getModId() {
        return "totem_used_renderer";
    }
}
