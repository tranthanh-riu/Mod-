package com.eldenringcore.registry;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.combat.AshOfWarType;
import com.eldenringcore.combat.EldenWeaponItem;
import com.eldenringcore.combat.StatusEffectType;
import com.eldenringcore.mechanic.CrimsonFlaskItem;
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

    // Boss Spawn Eggs (Waves 1-4)
    public static final RegistryObject<Item> MARGIT_SPAWN_EGG = ITEMS.register("margit_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MARGIT, 0x3D352E, 0xD4AF37, new Item.Properties()));
    public static final RegistryObject<Item> TREE_SENTINEL_SPAWN_EGG = ITEMS.register("tree_sentinel_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TREE_SENTINEL, 0xC9A227, 0x5C4813, new Item.Properties()));
    public static final RegistryObject<Item> GODRICK_SPAWN_EGG = ITEMS.register("godrick_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GODRICK, 0x6B1D1D, 0xD4AF37, new Item.Properties()));
    public static final RegistryObject<Item> RENNALA_SPAWN_EGG = ITEMS.register("rennala_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.RENNALA, 0x1A2A4A, 0x7EC8E3, new Item.Properties()));
    public static final RegistryObject<Item> RADAHN_SPAWN_EGG = ITEMS.register("radahn_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.RADAHN, 0x4A1828, 0x9B59B6, new Item.Properties()));
    public static final RegistryObject<Item> MORGOTT_SPAWN_EGG = ITEMS.register("morgott_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MORGOTT, 0x4E4234, 0xFFD700, new Item.Properties()));
    public static final RegistryObject<Item> RYKARD_SPAWN_EGG = ITEMS.register("rykard_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.RYKARD, 0x5A1A1A, 0xFF5722, new Item.Properties()));
    public static final RegistryObject<Item> MOHG_SPAWN_EGG = ITEMS.register("mohg_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MOHG, 0x1A0A0A, 0xB71C1C, new Item.Properties()));
    public static final RegistryObject<Item> FIRE_GIANT_SPAWN_EGG = ITEMS.register("fire_giant_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.FIRE_GIANT, 0x8D3B24, 0xFF9800, new Item.Properties()));
    public static final RegistryObject<Item> MALENIA_SPAWN_EGG = ITEMS.register("malenia_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MALENIA, 0xC62828, 0xFFD54F, new Item.Properties()));
    public static final RegistryObject<Item> MALIKETH_SPAWN_EGG = ITEMS.register("maliketh_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MALIKETH, 0x121212, 0xD32F2F, new Item.Properties()));
    public static final RegistryObject<Item> GODFREY_SPAWN_EGG = ITEMS.register("godfrey_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GODFREY, 0xD7CCC8, 0xFFC107, new Item.Properties()));
    public static final RegistryObject<Item> RADAGON_SPAWN_EGG = ITEMS.register("radagon_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.RADAGON, 0x262626, 0xFFE082, new Item.Properties()));
}
