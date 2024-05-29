package prelude.protocol.server;

import prelude.protocol.Packet;
import prelude.protocol.PacketManager;
import prelude.protocol.ProcessedResult;

import java.util.regex.Pattern;

public abstract class ServerBoundPacket extends Packet {
    protected ServerBoundPacket() {
        PacketManager.serverBoundPackets.add(this);
    }

    public abstract ProcessedResult processPacket(ServerPacketManager manager);

    public abstract ServerBoundPacket createNewInstanceWithData(String data);

    public abstract Pattern getPattern();
}
