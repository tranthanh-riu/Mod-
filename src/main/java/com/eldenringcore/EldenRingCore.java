package com.eldenringcore;

import com.eldenringcore.boss.*;
import com.eldenringcore.combat.StatusBuildupManager;
import com.eldenringcore.mechanic.EldenStoryCommand;
import com.eldenringcore.mechanic.RegionDifficultyManager;
import com.eldenringcore.registry.ModBlockEntities;
import com.eldenringcore.registry.ModBlocks;
import com.eldenringcore.registry.ModCreativeTabs;
import com.eldenringcore.registry.ModEntities;
import com.eldenringcore.registry.ModItems;
import com.eldenringcore.registry.ModWorldgen;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
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
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModWorldgen.BIOME_SOURCES.register(modEventBus);
        com.eldenringcore.registry.ModEffects.EFFECTS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) -> event.enqueueWork(com.eldenringcore.network.ModNetwork::register));

        MinecraftForge.EVENT_BUS.register(new StatusBuildupManager());
        MinecraftForge.EVENT_BUS.register(new RegionDifficultyManager());
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> EldenStoryCommand.register(event.getDispatcher()));

        LOGGER.info("Elden Ring Core initialized with 13 Elden Ring Bosses (Waves 1-4).");
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        // Wave 1
        event.put(ModEntities.MARGIT.get(), MargitEntity.createAttributes().build());
        event.put(ModEntities.TREE_SENTINEL.get(), TreeSentinelEntity.createAttributes().build());
        event.put(ModEntities.GODRICK.get(), GodrickEntity.createAttributes().build());
        // Wave 2
        event.put(ModEntities.RENNALA.get(), RennalaEntity.createAttributes().build());
        event.put(ModEntities.RADAHN.get(), RadahnEntity.createAttributes().build());
        // Wave 3
        event.put(ModEntities.MORGOTT.get(), MorgottEntity.createAttributes().build());
        event.put(ModEntities.RYKARD.get(), RykardEntity.createAttributes().build());
        event.put(ModEntities.MOHG.get(), MohgEntity.createAttributes().build());
        event.put(ModEntities.FIRE_GIANT.get(), FireGiantEntity.createAttributes().build());
        // Wave 4
        event.put(ModEntities.MALENIA.get(), MaleniaEntity.createAttributes().build());
        event.put(ModEntities.MALIKETH.get(), MalikethEntity.createAttributes().build());
        event.put(ModEntities.GODFREY.get(), GodfreyEntity.createAttributes().build());
        event.put(ModEntities.RADAGON.get(), RadagonEntity.createAttributes().build());
    }
}
