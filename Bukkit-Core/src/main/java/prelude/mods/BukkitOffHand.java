package prelude.mods;

import prelude.PreludePlugin;
import prelude.api.PreludePlayer;
import prelude.api.Prelude;
import prelude.api.mods.OffHand;

import java.io.IOException;

public final class BukkitOffHand extends OffHand {

    public BukkitOffHand() {
        super();
        Prelude.getInstance().addMod(this);

        enabled = true;
    }

    @Override
    public boolean isAllowed() {
        return PreludePlugin.getInstance().getModConfig().getBoolean("off-hand.allowed", true);
    }

    @Override
    public boolean isOfficiallyHooked() {
        return true;
    }

    @Override
    public void sendOffhandEvent(PreludePlayer preludePlayer, String serializedItem, boolean canClientIgnore) throws IOException {
        super.sendOffhandEvent(preludePlayer, serializedItem, canClientIgnore);
        PreludePlugin.getInstance().debug("Dispatched UpdateOffhandEvent to " + preludePlayer);
    }
}
