package prelude.mods;

import prelude.PreludePlugin;
import prelude.api.PreludePlayer;
import prelude.api.Prelude;
import prelude.api.mods.ServerTps;

import java.io.IOException;

public final class BukkitServerTps extends ServerTps {

    public BukkitServerTps() {
        super();
        Prelude.getInstance().addMod(this);
        enabled = true;
    }

    @Override
    public void sendServerTpsUpdate(PreludePlayer preludePlayer, double currentTps) throws IOException {
        super.sendServerTpsUpdate(preludePlayer, currentTps);
        PreludePlugin.getInstance().debug("Dispatched ServerTpsUpdate to " + preludePlayer.getUsername());
    }

    @Override
    public boolean isAllowed() {
        return PreludePlugin.getInstance().getModConfig().getBoolean("server-tps.allowed", true);
    }

    @Override
    public boolean isOfficiallyHooked() {
        return true;
    }
}
