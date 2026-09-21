package alku.phayriosis_cataclysm.init;

import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Phayriosis_cataclysm.MODID);

    public static final RegistryObject<SoundEvent> SWARM_CALL = registerSound("swarm_call");
    public static final RegistryObject<SoundEvent> STAGE_ADVANCE = registerSound("stage_advance");
    public static final RegistryObject<SoundEvent> SPORE_FIRE = registerSound("spore_fire");
    public static final RegistryObject<SoundEvent> MERGE_START = registerSound("merge_start");
    public static final RegistryObject<SoundEvent> INFECTION_CORE_PULSE = registerSound("infection_core_pulse");
    public static final RegistryObject<SoundEvent> CARAPACE_ACTIVATE = registerSound("carapace_activate");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(Phayriosis_cataclysm.MODID, name)));
    }
}
