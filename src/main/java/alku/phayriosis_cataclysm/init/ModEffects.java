package alku.phayriosis_cataclysm.init;

import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.effect.HemolysisMobEffect;
import alku.phayriosis_cataclysm.effect.HolyMobEffect;
import alku.phayriosis_cataclysm.effect.StunMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Phayriosis_cataclysm.MODID);

    // Hemolysis - stacking true damage inflicted by PHA creatures
    public static final RegistryObject<MobEffect> HEMOLYSIS =
            MOB_EFFECTS.register("hemolysis", HemolysisMobEffect::new);

    // Holy - Dreadmind blessing: 150% attributes, marks victims, takes true damage from PHA
    public static final RegistryObject<MobEffect> HOLY =
            MOB_EFFECTS.register("holy", HolyMobEffect::new);

    // Stun - Dreadmind meteor impact: completely immobilised, cannot move, jump or attack
    public static final RegistryObject<MobEffect> STUN =
            MOB_EFFECTS.register("stun", StunMobEffect::new);
}
