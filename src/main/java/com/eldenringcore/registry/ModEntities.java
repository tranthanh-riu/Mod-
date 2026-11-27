package com.eldenringcore.registry;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.boss.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EldenRingCore.MOD_ID);

    // Wave 1: Limgrave & Stormveil
    public static final RegistryObject<EntityType<MargitEntity>> MARGIT = registerBoss("margit", MargitEntity::new, 1.1f, 3.2f);
    public static final RegistryObject<EntityType<TreeSentinelEntity>> TREE_SENTINEL = registerBoss("tree_sentinel", TreeSentinelEntity::new, 1.6f, 3.8f);
    public static final RegistryObject<EntityType<GodrickEntity>> GODRICK = registerBoss("godrick", GodrickEntity::new, 1.5f, 4.0f);

    // Wave 2: Liurnia & Caelid
    public static final RegistryObject<EntityType<RennalaEntity>> RENNALA = registerBoss("rennala", RennalaEntity::new, 1.0f, 2.8f);
    public static final RegistryObject<EntityType<RadahnEntity>> RADAHN = registerBoss("radahn", RadahnEntity::new, 2.4f, 5.5f);

    // Wave 3: Leyndell, Volcano, Mohgwyn & Mountaintops
    public static final RegistryObject<EntityType<MorgottEntity>> MORGOTT = registerBoss("morgott", MorgottEntity::new, 1.1f, 3.3f);
    public static final RegistryObject<EntityType<RykardEntity>> RYKARD = registerBoss("rykard", RykardEntity::new, 2.8f, 5.2f);
    public static final RegistryObject<EntityType<MohgEntity>> MOHG = registerBoss("mohg", MohgEntity::new, 1.5f, 4.2f);
    public static final RegistryObject<EntityType<FireGiantEntity>> FIRE_GIANT = registerBoss("fire_giant", FireGiantEntity::new, 3.2f, 7.5f);

    // Wave 4: Endgame
    public static final RegistryObject<EntityType<MaleniaEntity>> MALENIA = registerBoss("malenia", MaleniaEntity::new, 0.9f, 2.6f);
    public static final RegistryObject<EntityType<MalikethEntity>> MALIKETH = registerBoss("maliketh", MalikethEntity::new, 1.4f, 3.4f);
    public static final RegistryObject<EntityType<GodfreyEntity>> GODFREY = registerBoss("godfrey", GodfreyEntity::new, 1.5f, 3.8f);
    public static final RegistryObject<EntityType<RadagonEntity>> RADAGON = registerBoss("radagon", RadagonEntity::new, 1.1f, 3.0f);

    private static <T extends EldenBossEntity> RegistryObject<EntityType<T>> registerBoss(
            String id, EntityType.EntityFactory<T> factory, float width, float height) {
        return ENTITY_TYPES.register(id, () -> EntityType.Builder.of(factory, MobCategory.MONSTER)
                .sized(width, height)
                .fireImmune()
                .clientTrackingRange(64)
                .build(id));
    }
}
