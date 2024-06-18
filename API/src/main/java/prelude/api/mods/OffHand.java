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
