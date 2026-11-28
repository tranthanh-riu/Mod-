package com.eldenringcore.nativecombat;

import com.eldenringcore.combat.StatusEffectType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Classic Minecraft 2D pixel-art style Elden Ring weapon powered by our Native Combat Engine + playerAnimator.
 * Prototype: Uchigatana (3-hit Katana combo, Heavy Thrust, Ash of War: Unsheathe, Bleed buildup, Dodge Roll).
 */
public class NativeKatanaItem extends SwordItem {
    private final EldenWeaponStyle style;
    private final StatusEffectType statusType;
    private final float statusBuildup;

    public NativeKatanaItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties props,
                            EldenWeaponStyle style, StatusEffectType statusType, float statusBuildup) {
        super(tier, attackDamageModifier, attackSpeedModifier, props);
        this.style = style;
        this.statusType = statusType;
        this.statusBuildup = statusBuildup;
    }

    public EldenWeaponStyle getStyle() {
        return style;
    }

    public StatusEffectType getStatusType() {
        return statusType;
    }

    public float getStatusBuildup() {
        return statusBuildup;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            NativeCombatNetworking.sendAction(C2SAction.UNSHEATHE, 0f, 0f);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Style: Katana (Native Combat — Không cần Epic Fight)")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("• Chuột trái: Combo chém 3 nhịp + Bleed (" + (int) statusBuildup + ")")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("• Phím X (hoặc Shift/Nhảy + Đánh): Đâm nặng phá Poise")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("• Chuột phải: Ash of War — Unsheathe (Rút kiếm chém sóng, 15 FP)")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("• Phím Alt trái: Dodge Roll lộn nhào (i-frames bất tử)")
                .withStyle(ChatFormatting.GREEN));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public enum C2SAction {
        LIGHT_COMBO,
        HEAVY_ATTACK,
        UNSHEATHE,
        DODGE_ROLL
    }
}
