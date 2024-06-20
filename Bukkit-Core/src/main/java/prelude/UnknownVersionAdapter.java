package prelude;

import prelude.adapter.VersionAdapter;
import prelude.api.mods.AnchorRenderer;
import prelude.api.mods.OffHand;
import prelude.api.mods.TotemUsedRenderer;

public class UnknownVersionAdapter implements VersionAdapter {
    @Override
    public void registerAnchorListener(AnchorRenderer anchorMod) {

    }

    @Override
    public void registerTotemListener(TotemUsedRenderer totemMod) {

    }

    @Override
    public void registerOffhandListeners(OffHand offHandMod) {

    }
}
