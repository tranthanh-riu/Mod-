package com.eldenringcore.client;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.boss.EldenBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Generic GeckoLib model loader for all Elden Ring Bosses:
 * - assets/eldenring_core/geo/<bossId>.geo.json
 * - assets/eldenring_core/textures/entity/<bossId>.png
 * - assets/eldenring_core/animations/<bossId>.animation.json
 */
public class EldenBossGeoModel<T extends EldenBossEntity> extends GeoModel<T> {
    private final ResourceLocation modelPath;
    private final ResourceLocation texturePath;
    private final ResourceLocation animationPath;

    public EldenBossGeoModel(String bossId) {
        this.modelPath = new ResourceLocation(EldenRingCore.MOD_ID, "geo/" + bossId + ".geo.json");
        this.texturePath = new ResourceLocation(EldenRingCore.MOD_ID, "textures/entity/" + bossId + ".png");
        this.animationPath = new ResourceLocation(EldenRingCore.MOD_ID, "animations/" + bossId + ".animation.json");
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return this.modelPath;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return this.texturePath;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return this.animationPath;
    }
}
