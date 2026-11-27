package com.eldenringcore;

import com.eldenringcore.boss.MargitEntity;
import com.eldenringcore.combat.StatusBuildupManager;
import com.eldenringcore.registry.ModBlocks;
import com.eldenringcore.registry.ModCreativeTabs;
import com.eldenringcore.registry.ModEntities;
import com.eldenringcore.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(EldenRingCore.MOD_ID)
public class EldenRingCore {
    public static final String MOD_ID = "eldenring_core";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EldenRingCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        GeckoLib.initialize();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);

        MinecraftForge.EVENT_BUS.register(new StatusBuildupManager());

        LOGGER.info("Elden Ring Core initialized for Minecraft 1.20.1 Forge.");
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MARGIT.get(), MargitEntity.createAttributes().build());
    }
}
