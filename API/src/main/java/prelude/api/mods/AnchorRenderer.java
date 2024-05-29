package prelude.api.mods;

import prelude.api.PreludePlayer;
import prelude.api.ResentMod;
import prelude.protocol.packets.clientbound.AnchorRendererPacket;
import prelude.protocol.packets.clientbound.AnchorRendererPacket.AnchorRendererPacketBuilder;

public abstract class AnchorRenderer extends ResentMod {
    protected AnchorRenderer() {
        super();
    }

    public void sendPlacedAnchorPacket(PreludePlayer preludePlayer, int x, int y, int z) {
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
    public void sendInteractedAnchorPacket(PreludePlayer preludePlayer, int x, int y, int z, int charge) {
        AnchorRendererPacketBuilder builder = AnchorRendererPacket.builder();

        preludePlayer.sendPacket(
                builder
                        .x(x)
                        .y(y)
                        .z(z)
                        .charge(charge + 1)
                        .receiver(this.getReceiverId())
                        .build()
        );
    }

    public void sendBlownUpAnchorPacket(PreludePlayer preludePlayer, int x, int y, int z) {
        sendInteractedAnchorPacket(preludePlayer, x, y, z, 0);
    }

    @Override
    public final String getReceiverId() {
        return "anchor_renderer";
    }
}
