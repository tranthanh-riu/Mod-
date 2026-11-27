package com.eldenringcore.mechanic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Site of Grace: Rest to restore full HP, refill all Crimson Flasks, and set respawn point.
 */
public class SiteOfGraceBlock extends Block {
    public SiteOfGraceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            boolean discovered = GraceLocations.discover(serverPlayer, pos);
            if (player.isShiftKeyDown() && !discovered) {
                GraceTravelMenu.open(serverPlayer, pos);
                return InteractionResult.CONSUME;
            }
            serverPlayer.setHealth(serverPlayer.getMaxHealth());
            serverPlayer.getFoodData().setFoodLevel(20);
            serverPlayer.setRespawnPosition(level.dimension(), pos.above(), 0.0f, true, false);

            for (int slot = 0; slot < serverPlayer.getInventory().getContainerSize(); slot++) {
                ItemStack stack = serverPlayer.getInventory().getItem(slot);
                if (stack.getItem() instanceof CrimsonFlaskItem || stack.getItem() instanceof CeruleanFlaskItem) {
                    CrimsonFlaskItem.refill(stack);
                }
            }

            if (discovered) {
                serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 15));
                serverPlayer.connection.send(new ClientboundSetTitleTextPacket(
                        Component.literal("LOST GRACE DISCOVERED").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
            }
            serverPlayer.displayClientMessage(Component.translatable("message.eldenring_core.grace.travel_hint"), true);

            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.2f);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 30, 0.4, 0.8, 0.4, 0.05);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    pos.getY() + 0.8,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    0.0, 0.04, 0.0);
        }
    }
}
