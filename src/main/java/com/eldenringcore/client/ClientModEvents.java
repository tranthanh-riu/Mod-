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
        event.registerEntityRenderer(ModEntities.MARGIT.get(), ctx -> new EldenBossRenderer<>(ctx, "margit"));
        event.registerEntityRenderer(ModEntities.TREE_SENTINEL.get(), ctx -> new EldenBossRenderer<>(ctx, "tree_sentinel"));
        event.registerEntityRenderer(ModEntities.GODRICK.get(), ctx -> new EldenBossRenderer<>(ctx, "godrick"));
    }
}
