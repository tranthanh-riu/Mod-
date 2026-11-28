package com.eldenringcore.registry;

import com.eldenringcore.EldenRingCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EldenRingCore.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ELDEN_RING_TAB = CREATIVE_MODE_TABS.register("elden_ring_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.eldenring_core"))
                    .icon(() -> new ItemStack(ModItems.RIVERS_OF_BLOOD.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.UCHIGATANA.get());
                        output.accept(ModItems.RIVERS_OF_BLOOD.get());
                        output.accept(ModItems.MOONVEIL.get());
                        output.accept(ModItems.DARK_MOON_GREATSWORD.get());
                        output.accept(ModItems.HAND_OF_MALENIA.get());
                        output.accept(ModItems.BLASPHEMOUS_BLADE.get());
                        output.accept(ModItems.STARSCOURGE_GREATSWORD.get());
                        output.accept(ModItems.MALIKETHS_BLACK_BLADE.get());
                        output.accept(ModItems.BLOODHOUNDS_FANG.get());
                        output.accept(ModItems.GOLDEN_HALBERD.get());
                        output.accept(ModItems.BOLT_OF_GRANSAX.get());
                        output.accept(ModItems.FLASK_OF_CRIMSON_TEARS.get());
                        output.accept(ModItems.FLASK_OF_CERULEAN_TEARS.get());
                        output.accept(ModItems.SMITHING_STONE.get());
                        output.accept(ModItems.SOMBER_SMITHING_STONE.get());
                        output.accept(ModItems.SITE_OF_GRACE_ITEM.get());
                        output.accept(ModItems.FOG_GATE_ITEM.get());
                        output.accept(ModItems.BOSS_ARENA_CONTROLLER_ITEM.get());
                        output.accept(ModItems.MARGIT_SPAWN_EGG.get());
                        output.accept(ModItems.TREE_SENTINEL_SPAWN_EGG.get());
                        output.accept(ModItems.GODRICK_SPAWN_EGG.get());
                        output.accept(ModItems.RENNALA_SPAWN_EGG.get());
                        output.accept(ModItems.RADAHN_SPAWN_EGG.get());
                        output.accept(ModItems.MORGOTT_SPAWN_EGG.get());
                        output.accept(ModItems.RYKARD_SPAWN_EGG.get());
                        output.accept(ModItems.MOHG_SPAWN_EGG.get());
                        output.accept(ModItems.FIRE_GIANT_SPAWN_EGG.get());
                        output.accept(ModItems.MALENIA_SPAWN_EGG.get());
                        output.accept(ModItems.MALIKETH_SPAWN_EGG.get());
                        output.accept(ModItems.GODFREY_SPAWN_EGG.get());
                        output.accept(ModItems.RADAGON_SPAWN_EGG.get());
                    })
                    .build());
}
