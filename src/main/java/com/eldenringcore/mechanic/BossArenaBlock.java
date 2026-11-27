package com.eldenringcore.mechanic;

import com.eldenringcore.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Adventure Map tool for Elden Ring Boss Arenas:
 * - Place inside a Boss Arena behind a Fog Gate.
 * - Spawns the configured boss when a Survival/Adventure player enters range.
 * - Automatically resets the boss if all players die or leave.
 * - Spawns a Site of Grace on victory!
 * - Right-click in Creative mode to reset the arena for testing.
 */
public class BossArenaBlock extends BaseEntityBlock {
    public BossArenaBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BossArenaBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.BOSS_ARENA.get(), BossArenaBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player.isCreative()) {
            if (level.getBlockEntity(pos) instanceof BossArenaBlockEntity arena) {
                arena.resetArena();
                player.sendSystemMessage(Component.literal("[Boss Arena] Reset! Boss: " + arena.getBossId())
                        .withStyle(ChatFormatting.GOLD));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
