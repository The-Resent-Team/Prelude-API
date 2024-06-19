package prelude;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import prelude.adapter.BukkitPlayerAdapter;
import prelude.api.Prelude;
import prelude.api.PreludePlayer;
import prelude.api.ResentMod;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class BukkitPrelude extends Prelude {

    private static final Set<ResentMod> mods = new HashSet<>();

    public BukkitPrelude() {
        setInstance(this);
        setC2SPacketHandler(new BukkitC2SPacketHandler());
    }

    @Override
    public PreludePlayer getActor(UUID uuid) throws IllegalStateException {
        return getPreludePlayer(uuid);
    }

    @Override
    public PreludePlayer getPreludePlayer(UUID uuid) throws IllegalStateException {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            throw new IllegalStateException("An actor must be online! Attempted UUID: " + uuid.toString());
        }
        return BukkitPlayerAdapter.getPreludePlayer(PreludePlugin.getInstance(), player);
    }

    @Override
    public void validateConnection(PreludePlayer preludePlayer) throws IOException {
        //Player player = Bukkit.getPlayer(preludePlayer.getUuid());


//        TODO
//        ServerHandshakePacket pkt = ServerHandshakePacket.builder().majorVersion().build()

        PreludePlugin.getInstance().debug("Validating mods for " + preludePlayer);
        for (ResentMod mod : mods) {
            if (!mod.isEnabled()) {
                PreludePlugin.getInstance().debug(String.format("Mod %s did not get enabled",
                        mod.getClass().getSimpleName()));
                continue;
            }
            if (!mod.isAllowed()) {
                mod.disableMod(preludePlayer);
                PreludePlugin.getInstance().debug(String.format("Mod %s is not allowed and was disabled for %s",
                        mod.getClass().getSimpleName(), preludePlayer.getUsername()));
                continue;
            }
            if (mod.isOfficiallyHooked()) {
                mod.initMod(preludePlayer);
            }
        }

        // TODO
//        if (PreludePlugin.getInstance().getConfig().getBoolean("patches.potion-effect-kick-patch.enabled", true)) {
//            if (player != null) {
//                for (PotionEffect effect : player.getActivePotionEffects()) {
//
//                }
//            }
//        }
    }

    @Override
    public <T extends ResentMod> Optional<T> getMod(Class<T> modClass) {
        Preconditions.checkArgument(Modifier.isFinal(modClass.getModifiers()));
        Preconditions.checkArgument(!Modifier.isAbstract(modClass.getModifiers()));

        return mods.stream()
                .filter(mod -> modClass.isAssignableFrom(mod.getClass()))
                .map(modClass::cast)
                .findFirst();
    }

    @Override
    public void addMod(ResentMod mod) {
        mods.add(mod);
    }

    @Override
    public Set<ResentMod> getMods() {
        return ImmutableSet.copyOf(mods);
    }
}
