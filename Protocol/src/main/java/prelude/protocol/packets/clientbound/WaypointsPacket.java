package prelude.protocol.packets.clientbound;

import lombok.Builder;
import prelude.protocol.packets.client.ClientBoundPacket;
import prelude.protocol.packets.client.ClientPacketManager;
import prelude.protocol.PacketManager;
import prelude.protocol.ProcessedResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Builder
public class WaypointsPacket extends ClientBoundPacket {
    @Builder.Default private List<Waypoint> waypoints = new ArrayList<>();
    private String receiver;

    @Override
    public String serialize() {
        StringBuilder b = new StringBuilder();

        int iMax = waypoints.size() - 1;
        if (iMax != -1) {
            for (int i = 0; ; i++) {
                b.append(waypoints.get(i).toString());
                if (i == iMax)
                    break;
                b.append(",");
            }
        } else {
            b.append("{}");
        }

        return GENERIC_PACKET_FORMAT
                .replace("%receiver%", "waypoints-data")
                .replace("%message%", b.toString());
    }

    @Override
    public ProcessedResult processPacket(ClientPacketManager manager) {
        return null;
    }

    @Override
    public ClientBoundPacket createNewInstanceWithData(String data) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaypointsPacket)) return false;
        WaypointsPacket that = (WaypointsPacket) o;
        return Objects.equals(waypoints, that.waypoints);
    }

    public static class Waypoint {
        private final String name;
        private final int x;
        private final int y;
        private final int z;

        public Waypoint(String name, int x, int y, int z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return "{x=" + x + ",y=" + y + ",z=" + z + ",name=" + name + "}";
        }
    }
}
