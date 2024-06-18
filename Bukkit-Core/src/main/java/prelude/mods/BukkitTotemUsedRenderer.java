package prelude.mods;

import prelude.PreludePlugin;
import prelude.api.PreludePlayer;
import prelude.api.Prelude;
import prelude.api.mods.TotemUsedRenderer;

public final class BukkitTotemUsedRenderer extends TotemUsedRenderer {

    public BukkitTotemUsedRenderer() {
        super();
        Prelude.getInstance().addMod(this);

        enabled = true;
    }

    @Override
    public boolean isAllowed() {
        return PreludePlugin.getInstance().getModConfig().getBoolean("totem-tweaks.allowed", true);
    }

    @Override
    public boolean isOfficiallyHooked() {
        return true;
    }

    @Override
    public void sendTotemPoppedEvent(PreludePlayer preludePlayer) {
        super.sendTotemPoppedEvent(preludePlayer);
        PreludePlugin.getInstance().debug("Dispatched TotemPoppedEvent to " + preludePlayer);
    }
}
