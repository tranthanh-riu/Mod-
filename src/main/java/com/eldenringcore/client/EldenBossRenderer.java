package com.eldenringcore.client;

import com.eldenringcore.boss.EldenBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EldenBossRenderer<T extends EldenBossEntity> extends GeoEntityRenderer<T> {
    public EldenBossRenderer(EntityRendererProvider.Context renderManager, String bossId) {
        super(renderManager, new EldenBossGeoModel<>(bossId));
        this.shadowRadius = 1.1f;
    }
}
