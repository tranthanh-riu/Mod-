package com.eldenringcore.combat;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Elden Ring Weapon Upgrade System:
 * - Somber Weapons (Epic EldenWeaponItem): upgraded +1 to +10 with Somber Smithing Stones.
 * - Standard Weapons (Rare/Standard EldenWeaponItem & Swords): upgraded +1 to +25 with Smithing Stones.
 * Works in an Anvil (weapon + stone) OR by Right-Clicking a Smithing Table with weapon in mainhand & stone in offhand.
 */
@Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WeaponUpgradeManager {
    public static final String TAG_UPGRADE_LEVEL = "eldenring_upgrade_level";
    public static final int MAX_STANDARD_LEVEL = 25;
    public static final int MAX_SOMBER_LEVEL = 10;

    private static final UUID UPGRADE_DAMAGE_UUID = UUID.fromString("e6b9c8a2-7b4d-4c1f-9a3e-2d8f4b6c1a90");

    public static boolean isSomberWeapon(ItemStack stack) {
        return stack.getItem() instanceof EldenWeaponItem && stack.getRarity() == Rarity.EPIC;
    }

    public static boolean isUpgradeableWeapon(ItemStack stack) {
        return stack.getItem() instanceof EldenWeaponItem || stack.getItem() instanceof SwordItem;
    }

    public static int getMaxLevel(ItemStack stack) {
        return isSomberWeapon(stack) ? MAX_SOMBER_LEVEL : MAX_STANDARD_LEVEL;
    }

    public static int getUpgradeLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_UPGRADE_LEVEL)) {
            return 0;
        }
        return Math.max(0, Math.min(tag.getInt(TAG_UPGRADE_LEVEL), getMaxLevel(stack)));
    }

    public static void setUpgradeLevel(ItemStack stack, int level) {
        int clamped = Math.max(0, Math.min(level, getMaxLevel(stack)));
        stack.getOrCreateTag().putInt(TAG_UPGRADE_LEVEL, clamped);
    }

    public static Item getRequiredStone(ItemStack stack) {
        return isSomberWeapon(stack) ? ModItems.SOMBER_SMITHING_STONE.get() : ModItems.SMITHING_STONE.get();
    }

    public static int getRequiredStoneCount(ItemStack stack, int currentLevel) {
        if (isSomberWeapon(stack)) {
            return 1;
        }
        return 1 + (currentLevel / 6); // 1 stone (+1..+6), 2 (+7..+12), 3 (+13..+18), 4 (+19..+24), 5 (+25)
    }

    public static double getBonusAttackDamage(ItemStack stack) {
        int level = getUpgradeLevel(stack);
        if (level <= 0) {
            return 0.0;
        }
        return isSomberWeapon(stack) ? level * 1.2 : level * 0.5;
    }

    public static float getAshDamageMultiplier(ItemStack stack) {
        int level = getUpgradeLevel(stack);
        if (level <= 0) {
            return 1.0f;
        }
        return isSomberWeapon(stack) ? (1.0f + level * 0.05f) : (1.0f + level * 0.02f);
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!isUpgradeableWeapon(stack)) {
            return;
        }
        double bonusDamage = getBonusAttackDamage(stack);
        if (bonusDamage > 0.0) {
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    UPGRADE_DAMAGE_UUID,
                    "Elden Ring Weapon Upgrade",
                    bonusDamage,
                    AttributeModifier.Operation.ADDITION));
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!isUpgradeableWeapon(left) || right.isEmpty()) {
            return;
        }

        Item requiredStone = getRequiredStone(left);
        if (!right.is(requiredStone)) {
            return;
        }

        int currentLevel = getUpgradeLevel(left);
        int maxLevel = getMaxLevel(left);
        if (currentLevel >= maxLevel) {
            return;
        }

        int stoneCost = getRequiredStoneCount(left, currentLevel);
        if (right.getCount() < stoneCost) {
            return;
        }

        ItemStack output = left.copy();
        setUpgradeLevel(output, currentLevel + 1);

        int xpCost = isSomberWeapon(left) ? (2 + currentLevel) : (1 + currentLevel / 3);
        event.setOutput(output);
        event.setCost(xpCost);
        event.setMaterialCost(stoneCost);
    }

    @SubscribeEvent
    public static void onSmithingTableInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!event.getLevel().getBlockState(event.getPos()).is(Blocks.SMITHING_TABLE)) {
            return;
        }

        Player player = event.getEntity();
        ItemStack weapon = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        if (!isUpgradeableWeapon(weapon) || offhand.isEmpty()) {
            return;
        }

        Item requiredStone = getRequiredStone(weapon);
        if (!offhand.is(requiredStone)) {
            return;
        }

        int currentLevel = getUpgradeLevel(weapon);
        int maxLevel = getMaxLevel(weapon);
        if (currentLevel >= maxLevel) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.literal("Weapon is already at maximum upgrade level (+" + maxLevel + ")")
                                .withStyle(ChatFormatting.YELLOW),
                        true);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        int stoneCost = getRequiredStoneCount(weapon, currentLevel);
        if (!player.getAbilities().instabuild && offhand.getCount() < stoneCost) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.literal("Requires " + stoneCost + "x " + requiredStone.getDescription().getString())
                                .withStyle(ChatFormatting.RED),
                        true);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            if (!player.getAbilities().instabuild) {
                offhand.shrink(stoneCost);
            }
            int nextLevel = currentLevel + 1;
            setUpgradeLevel(weapon, nextLevel);

            serverLevel.playSound(null, event.getPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.1f);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    event.getPos().getX() + 0.5, event.getPos().getY() + 1.1, event.getPos().getZ() + 0.5,
                    14, 0.3, 0.2, 0.3, 0.04);
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    event.getPos().getX() + 0.5, event.getPos().getY() + 1.2, event.getPos().getZ() + 0.5,
                    18, 0.4, 0.3, 0.4, 0.1);

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.literal("Upgraded to +" + nextLevel + "!")
                                .withStyle(ChatFormatting.GOLD),
                        true);
            }
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
    }
}
