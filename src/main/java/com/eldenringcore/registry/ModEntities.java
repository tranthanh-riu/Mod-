package com.eldenringcore.registry;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.boss.GodrickEntity;
import com.eldenringcore.boss.MargitEntity;
import com.eldenringcore.boss.TreeSentinelEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EldenRingCore.MOD_ID);

    public static final RegistryObject<EntityType<MargitEntity>> MARGIT = ENTITY_TYPES.register("margit",
            () -> EntityType.Builder.of(MargitEntity::new, MobCategory.MONSTER)
                    .sized(1.1f, 3.2f)
                    .fireImmune()
                    .clientTrackingRange(64)
                    .build("margit"));

    public static final RegistryObject<EntityType<TreeSentinelEntity>> TREE_SENTINEL = ENTITY_TYPES.register("tree_sentinel",
            () -> EntityType.Builder.of(TreeSentinelEntity::new, MobCategory.MONSTER)
                    .sized(1.6f, 3.8f)
                    .fireImmune()
                    .clientTrackingRange(64)
                    .build("tree_sentinel"));

    public static final RegistryObject<EntityType<GodrickEntity>> GODRICK = ENTITY_TYPES.register("godrick",
            () -> EntityType.Builder.of(GodrickEntity::new, MobCategory.MONSTER)
                    .sized(1.5f, 4.0f)
                    .fireImmune()
                    .clientTrackingRange(64)
                    .build("godrick"));
}
