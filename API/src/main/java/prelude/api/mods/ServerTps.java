package prelude.api.mods;

import prelude.api.PreludePlayer;
import prelude.api.ResentMod;
import prelude.protocol.packets.s2c.ServerTpsPacket;

import java.io.IOException;

public abstract class ServerTps extends ResentMod {
    protected ServerTps() {
        super();
    }

    public void sendServerTpsUpdate(PreludePlayer preludePlayer, double currentTps) throws IOException {
        int floor = (int) Math.floor(currentTps);
        int mantissaToFourDigits = (int) (Math.floor(currentTps * 10000) - floor * 10000);

        preludePlayer.sendPacket(
                ServerTpsPacket.builder()
                        .characteristic((byte) floor)
                        .mantissa(mantissaToFourDigits)
                        .build()
        );
    }

    @Override
    public final String getModId() {
        return "server_tps";
    }
}
