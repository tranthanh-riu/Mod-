package com.eldenringcore.client;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.boss.EldenBossEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Generic GeckoLib model loader for all Elden Ring Bosses:
 * Automatically uses assets/eldenring_core/geo/<bossId>.geo.json (and matching texture/animation)
 * when provided by the Art team, and gracefully falls back to boss_placeholder for immediate testing!
 */
public class EldenBossGeoModel<T extends EldenBossEntity> extends GeoModel<T> {
    private static final ResourceLocation FALLBACK_MODEL = new ResourceLocation(EldenRingCore.MOD_ID, "geo/boss_placeholder.geo.json");
    private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation(EldenRingCore.MOD_ID, "textures/entity/boss_placeholder.png");
    private static final ResourceLocation FALLBACK_ANIMATION = new ResourceLocation(EldenRingCore.MOD_ID, "animations/boss_placeholder.animation.json");

    private final ResourceLocation modelPath;
    private final ResourceLocation texturePath;
    private final ResourceLocation animationPath;

    public EldenBossGeoModel(String bossId) {
        this.modelPath = new ResourceLocation(EldenRingCore.MOD_ID, "geo/" + bossId + ".geo.json");
        this.texturePath = new ResourceLocation(EldenRingCore.MOD_ID, "textures/entity/" + bossId + ".png");
        this.animationPath = new ResourceLocation(EldenRingCore.MOD_ID, "animations/" + bossId + ".animation.json");
    }

    private ResourceLocation resolveOrFallback(ResourceLocation primary, ResourceLocation fallback) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getResourceManager().getResource(primary).isPresent()) {
            return primary;
        }
        return fallback;
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return resolveOrFallback(this.modelPath, FALLBACK_MODEL);
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return resolveOrFallback(this.texturePath, FALLBACK_TEXTURE);
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return resolveOrFallback(this.animationPath, FALLBACK_ANIMATION);
    }
}
