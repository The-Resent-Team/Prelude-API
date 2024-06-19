package prelude.mods;

import prelude.PreludePlugin;
import prelude.api.PreludePlayer;
import prelude.api.Prelude;
import prelude.api.mods.AnchorRenderer;

import java.io.IOException;

public final class BukkitAnchorRenderer extends AnchorRenderer {
    public BukkitAnchorRenderer() {
        super();
        Prelude.getInstance().addMod(this);

        enabled = true;
    }

    @Override
    public boolean isAllowed() {
        return PreludePlugin.getInstance().getModConfig().getBoolean("anchor-renderer.allowed", true);
    }

    @Override
    public boolean isOfficiallyHooked() {
        return true;
    }

    @Override
    public void sendPlacedAnchorPacket(PreludePlayer preludePlayer, int x, int y, int z) throws IOException {
        super.sendPlacedAnchorPacket(preludePlayer, x, y, z);
        PreludePlugin.getInstance().debug("Dispatched PlacedAnchorEvent to " + preludePlayer);
    }

    @Override
    public void sendInteractedAnchorPacket(PreludePlayer preludePlayer, int x, int y, int z, int charge) throws IOException {
        super.sendInteractedAnchorPacket(preludePlayer, x, y, z, charge);
        PreludePlugin.getInstance().debug("Dispatched InteractedAnchorEvent to " + preludePlayer);
    }
}
