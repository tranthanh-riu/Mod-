package com.eldenringcore.registry;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.combat.AshOfWarType;
import com.eldenringcore.combat.EldenWeaponItem;
import com.eldenringcore.combat.StatusEffectType;
import com.eldenringcore.mechanic.CrimsonFlaskItem;
import com.eldenringcore.mechanic.CeruleanFlaskItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EldenRingCore.MOD_ID);

    // Iconic Elden Ring Weapons with Ash of War & Status Build-up
    public static final RegistryObject<Item> RIVERS_OF_BLOOD = ITEMS.register("rivers_of_blood",
            () -> new EldenWeaponItem(Tiers.NETHERITE, 5, -2.1f,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant(),
                    AshOfWarType.CORPSE_PILER, StatusEffectType.BLEED, 28.0f, 18.0f));

    public static final RegistryObject<Item> MOONVEIL = ITEMS.register("moonveil",
            () -> new EldenWeaponItem(Tiers.NETHERITE, 5, -2.0f,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant(),
                    AshOfWarType.TRANSIENT_MOONLIGHT, StatusEffectType.BLEED, 18.0f, 20.0f));

    public static final RegistryObject<Item> DARK_MOON_GREATSWORD = ITEMS.register("dark_moon_greatsword",
            () -> new EldenWeaponItem(Tiers.NETHERITE, 8, -2.8f,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant(),
                    AshOfWarType.MOONLIGHT_WAVE, StatusEffectType.FROSTBITE, 32.0f, 34.0f));

    public static final RegistryObject<Item> HAND_OF_MALENIA = ITEMS.register("hand_of_malenia",
            () -> new EldenWeaponItem(Tiers.NETHERITE, 6, -1.9f,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant(),
                    AshOfWarType.WATERFOWL_DANCE, StatusEffectType.BLEED, 22.0f, 22.0f));

    // Flask & Upgrade Materials
    public static final RegistryObject<Item> FLASK_OF_CRIMSON_TEARS = ITEMS.register("flask_of_crimson_tears",
            () -> new CrimsonFlaskItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SMITHING_STONE = ITEMS.register("smithing_stone",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SOMBER_SMITHING_STONE = ITEMS.register("somber_smithing_stone",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    // Block Items
    public static final RegistryObject<Item> SITE_OF_GRACE_ITEM = ITEMS.register("site_of_grace",
            () -> new BlockItem(ModBlocks.SITE_OF_GRACE.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> FOG_GATE_ITEM = ITEMS.register("fog_gate",
            () -> new BlockItem(ModBlocks.FOG_GATE.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> BOSS_ARENA_CONTROLLER_ITEM = ITEMS.register("boss_arena_controller",
            () -> new BlockItem(ModBlocks.BOSS_ARENA_CONTROLLER.get(), new Item.Properties().rarity(Rarity.EPIC)));

    // Boss Spawn Egg (for testing)
    public static final RegistryObject<Item> MARGIT_SPAWN_EGG = ITEMS.register("margit_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MARGIT, 0x3D352E, 0xD4AF37, new Item.Properties()));

    public static final RegistryObject<Item> FLASK_OF_CERULEAN_TEARS = ITEMS.register("flask_of_cerulean_tears",
            () -> new CeruleanFlaskItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
}
