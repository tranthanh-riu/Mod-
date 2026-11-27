package com.eldenringcore.client;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Wave 1
        event.registerEntityRenderer(ModEntities.MARGIT.get(), ctx -> new EldenBossRenderer<>(ctx, "margit"));
        event.registerEntityRenderer(ModEntities.TREE_SENTINEL.get(), ctx -> new EldenBossRenderer<>(ctx, "tree_sentinel"));
        event.registerEntityRenderer(ModEntities.GODRICK.get(), ctx -> new EldenBossRenderer<>(ctx, "godrick"));
        // Wave 2
        event.registerEntityRenderer(ModEntities.RENNALA.get(), ctx -> new EldenBossRenderer<>(ctx, "rennala"));
        event.registerEntityRenderer(ModEntities.RADAHN.get(), ctx -> new EldenBossRenderer<>(ctx, "radahn"));
        // Wave 3
        event.registerEntityRenderer(ModEntities.MORGOTT.get(), ctx -> new EldenBossRenderer<>(ctx, "morgott"));
        event.registerEntityRenderer(ModEntities.RYKARD.get(), ctx -> new EldenBossRenderer<>(ctx, "rykard"));
        event.registerEntityRenderer(ModEntities.MOHG.get(), ctx -> new EldenBossRenderer<>(ctx, "mohg"));
        event.registerEntityRenderer(ModEntities.FIRE_GIANT.get(), ctx -> new EldenBossRenderer<>(ctx, "fire_giant"));
        // Wave 4 (Malenia uses authentic low-poly 3D mesh with Phase 1 -> Phase 2 Goddess of Rot wing transformation!)
        event.registerEntityRenderer(ModEntities.MALENIA.get(),
                ctx -> new MeshBossRenderer<>(ctx, "malenia_real.png", "malenia_phase1.mesh", "malenia_phase2.mesh"));
        event.registerEntityRenderer(ModEntities.MALIKETH.get(), ctx -> new EldenBossRenderer<>(ctx, "maliketh"));
        event.registerEntityRenderer(ModEntities.GODFREY.get(), ctx -> new EldenBossRenderer<>(ctx, "godfrey"));
        event.registerEntityRenderer(ModEntities.RADAGON.get(), ctx -> new EldenBossRenderer<>(ctx, "radagon"));
    }
}
