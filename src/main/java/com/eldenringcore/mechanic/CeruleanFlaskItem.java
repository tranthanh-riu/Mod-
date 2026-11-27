package com.eldenringcore.mechanic;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CeruleanFlaskItem extends Item {
    private static final float FP_PER_CHARGE = 35.0f;

    // FocusPointsManager on the combat branch owns this persisted player data.
    private static final String FP_TAG = "eldenring_core_fp";

    public CeruleanFlaskItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int charges = CrimsonFlaskItem.getCharges(stack);
        if (charges <= 0) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            CompoundTag fp = player.getPersistentData().getCompound(FP_TAG);
            float current = fp.getFloat("current");
            float max = fp.getFloat("max");
            if (max <= 0.0f || !Float.isFinite(max) || !Float.isFinite(current) || current >= max) {
                return InteractionResultHolder.fail(stack);
            }

            float restored = Math.min(max, Math.max(0.0f, current) + FP_PER_CHARGE);
            fp.putFloat("current", restored);
            stack.getOrCreateTag().putInt(CrimsonFlaskItem.TAG_CHARGES, charges - 1);
            player.getCooldowns().addCooldown(this, 25);
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.displayClientMessage(Component.translatable("message.eldenring_core.cerulean.fp", Math.round(restored), Math.round(max))
                    .withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.eldenring_core.cerulean.charges", CrimsonFlaskItem.getCharges(stack), CrimsonFlaskItem.MAX_CHARGES)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.eldenring_core.cerulean.refill").withStyle(ChatFormatting.GRAY));
    }
}
