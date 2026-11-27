package com.eldenringcore.combat;

import com.eldenringcore.EldenRingCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * Balances armor sets from the external "Elden Ring Nightreign Gear" (nightreign_armor) 1.20.1 mod
 * so they fit Elden Ring Core's Boss Wave 1-4 damage curve without modifying the external jar.
 *
 * Original Nightreign Gear v1.5 totals:
 * - Recluse: 13 armor, 0 toughness (Light caster)
 * - Duchess: 15 armor, 4 toughness (Light/Med assassin)
 * - Ironeye: 19 armor, 2 toughness (Medium archer)
 * - Executor: 19 armor, 4 toughness (Medium swordsman)
 * - Wylder: 23 armor, 0 toughness (Medium vanguard)
 * - Guardian: 31 armor, 8 toughness -> rebalanced to 24 armor
 * - Raider: 31 armor, 12 toughness -> rebalanced to 26 armor
 */
@Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NightreignArmorBalancer {
    private static final String NIGHTREIGN_MOD_ID = "nightreign_armor";

    private static final UUID[] ARMOR_BALANCE_UUIDS = {
            UUID.fromString("c1a1b2c3-1111-4000-8000-000000000001"), // FEET
            UUID.fromString("c1a1b2c3-2222-4000-8000-000000000002"), // LEGS
            UUID.fromString("c1a1b2c3-3333-4000-8000-000000000003"), // CHEST
            UUID.fromString("c1a1b2c3-4444-4000-8000-000000000004")  // HEAD
    };

    @SubscribeEvent
    public static void onArmorAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ArmorItem armor)) {
            return;
        }
        EquipmentSlot slot = event.getSlotType();
        if (!slot.isArmor() || armor.getEquipmentSlot() != slot) {
            return;
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !NIGHTREIGN_MOD_ID.equals(id.getNamespace())) {
            return;
        }

        String path = id.getPath();
        double armorAdjustment = 0.0;

        if (path.startsWith("guardian_set_")) {
            // 31 -> 24 total armor (-1.75 per piece)
            armorAdjustment = -1.75;
        } else if (path.startsWith("raider_set_")) {
            // 31 -> 26 total armor (-1.25 per piece)
            armorAdjustment = -1.25;
        } else if (path.startsWith("wylder_set_")) {
            // 23 -> 21 total armor (-0.5 per piece)
            armorAdjustment = -0.5;
        }

        if (Math.abs(armorAdjustment) > 1.0E-4) {
            UUID uuid = ARMOR_BALANCE_UUIDS[slot.getIndex()];
            event.addModifier(Attributes.ARMOR, new AttributeModifier(
                    uuid,
                    "Elden Ring Core Nightreign Balance",
                    armorAdjustment,
                    AttributeModifier.Operation.ADDITION));
        }
    }
}
