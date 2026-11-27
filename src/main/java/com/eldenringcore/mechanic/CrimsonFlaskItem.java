package com.eldenringcore.mechanic;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Flask of Crimson Tears: limited charges (default 5), refilled when resting at a Site of Grace.
 */
public class CrimsonFlaskItem extends Item {
    public static final String TAG_CHARGES = "Charges";
    public static final int MAX_CHARGES = 5;

    public CrimsonFlaskItem(Properties properties) {
        super(properties);
    }

    public static int getCharges(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_CHARGES)) {
            return MAX_CHARGES;
        }
        return tag.getInt(TAG_CHARGES);
    }

    public static void refill(ItemStack stack) {
        stack.getOrCreateTag().putInt(TAG_CHARGES, MAX_CHARGES);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int charges = getCharges(stack);
        if (charges <= 0 || player.getHealth() >= player.getMaxHealth()) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            player.heal(10.0f);
            stack.getOrCreateTag().putInt(TAG_CHARGES, charges - 1);
            player.getCooldowns().addCooldown(this, 25);
            player.playSound(SoundEvents.GENERIC_DRINK, 1.0f, 1.0f);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Charges: " + getCharges(stack) + " / " + MAX_CHARGES)
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Rest at a Site of Grace to refill.").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
