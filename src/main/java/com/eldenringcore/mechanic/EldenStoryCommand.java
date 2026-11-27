package com.eldenringcore.mechanic;

import com.eldenringcore.registry.ModWorldgen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * /eldenstory command for Storyline Progression, Fast Travel & Map Generation:
 * - /eldenstory status             : Show current Elden Ring Chapter, Great Runes & next objective
 * - /eldenstory graces             : Open clickable Site of Grace Fast Travel menu
 * - /eldenstory tp <index>         : Fast travel to discovered Site of Grace #index
 * - /eldenstory build_limgrave     : Build Chapter 1-2 route (Limgrave -> Stormveil -> Erdtree)
 * - /eldenstory build_all_chapters : Build all 7 Chapters & all 13 Boss Arenas + Fast Travel Graces!
 * - /eldenstory build_catacomb     : Build an underground Elden Ring Catacomb with Smithing Stone loot
 * - /eldenstory reset              : Reset story progression to Chapter 1
 */
public class EldenStoryCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("eldenstory")
                .then(Commands.literal("status").executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    StoryProgressionManager story = StoryProgressionManager.get(level);
                    int ch = story.getCurrentChapter();
                    ctx.getSource().sendSuccess(() -> Component.literal("=== ELDEN RING STORYLINE ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(story.getChapterTitle(ch)).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("Great Runes: " + story.getGreatRuneCount() + " / 7").withStyle(ChatFormatting.GOLD), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("Mục tiêu: " + story.getCurrentObjective()).withStyle(ChatFormatting.WHITE), false);
                    return 1;
                }))
                .then(Commands.literal("enter_world").executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                        ServerLevel landsBetween = player.server.getLevel(ModWorldgen.LANDS_BETWEEN_LEVEL);
                        if (landsBetween == null) {
                            ctx.getSource().sendFailure(Component.literal("Chưa nạp được Dimension eldenring_core:lands_between! Hãy tạo thế giới mới (New World) để nạp 7 vùng đất Elden Ring."));
                            return 0;
                        }
                        String dim = ModWorldgen.LANDS_BETWEEN_LEVEL.location().toString();
                        GraceFastTravelManager graces = GraceFastTravelManager.get(landsBetween);
                        graces.registerGrace(new BlockPos(-300, 90, 700),   "Vùng 1 — Limgrave (Chương 1-2)", dim, true);
                        graces.registerGrace(new BlockPos(-750, 90, -150),  "Vùng 2 — Liurnia of the Lakes (Chương 3)", dim, true);
                        graces.registerGrace(new BlockPos(650, 90, 550),    "Vùng 3 — Caelid - Vùng Đất Đỏ (Chương 3)", dim, true);
                        graces.registerGrace(new BlockPos(50, 90, -700),    "Vùng 4 — Altus Plateau / Leyndell (Chương 4)", dim, true);
                        graces.registerGrace(new BlockPos(-750, 90, -950),  "Vùng 5 — Mt. Gelmir / Volcano Manor (Chương 4)", dim, true);
                        graces.registerGrace(new BlockPos(800, 90, -1050),  "Vùng 6 — Mountaintops of the Giants (Chương 5)", dim, true);
                        graces.registerGrace(new BlockPos(0, 90, -1450),    "Vùng 7 — Haligtree / Far North (Chương 6-7)", dim, true);

                        landsBetween.getChunk(new BlockPos(-300, 80, 700));
                        int y = Math.max(landsBetween.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, -300, 700) + 2, 75);
                        player.teleportTo(landsBetween, -300.5, y, 700.5, 180.0f, 0.0f);
                        player.sendSystemMessage(Component.literal("Chào mừng tới THE LANDS BETWEEN (7 Vùng Đất Elden Ring + Hang Động + Hầm Ngục)!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                        graces.sendFastTravelMenu(player);
                    }
                    return 1;
                }))
                .then(Commands.literal("graces").executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                        GraceFastTravelManager.get(ctx.getSource().getLevel()).sendFastTravelMenu(player);
                    }
                    return 1;
                }))
                .then(Commands.literal("tp")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1)).executes(ctx -> {
                            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                int idx = IntegerArgumentType.getInteger(ctx, "index");
                                if (!GraceFastTravelManager.get(ctx.getSource().getLevel()).teleportPlayer(player, idx)) {
                                    ctx.getSource().sendFailure(Component.literal("Không tìm thấy Site of Grace số " + idx));
                                }
                            }
                            return 1;
                        })))
                .then(Commands.literal("build_limgrave").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    BlockPos origin = BlockPos.containing(ctx.getSource().getPosition());
                    LandsBetweenBuilder.buildLimgraveToStormveilRoute(level, origin);
                    ctx.getSource().sendSuccess(() -> Component.literal("Đã dựng Chương 1-2 (Limgrave → Stormveil → Erdtree)! Gõ /eldenstory graces để dịch chuyển.").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
                    return 1;
                }))
                .then(Commands.literal("build_all_chapters").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    BlockPos origin = BlockPos.containing(ctx.getSource().getPosition());
                    LandsBetweenBuilder.buildAllSevenChapters(level, origin);
                    ctx.getSource().sendSuccess(() -> Component.literal("Đã dựng trọn bộ 7 CHƯƠNG & 13 ĐẤU TRƯỜNG BOSS + toàn bộ điểm Site of Grace Fast Travel! Gõ /eldenstory graces để dịch chuyển giữa các Chương!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
                    return 1;
                }))
                .then(Commands.literal("build_catacomb").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    BlockPos origin = BlockPos.containing(ctx.getSource().getPosition());
                    LandsBetweenBuilder.buildCatacomb(level, origin);
                    ctx.getSource().sendSuccess(() -> Component.literal("Đã dựng 1 Hầm Mộ (Elden Ring Catacomb) kèm rương Smithing Stones ngay phía trước bạn!").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), true);
                    return 1;
                }))
                .then(Commands.literal("reset").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    StoryProgressionManager.get(ctx.getSource().getLevel()).reset();
                    ctx.getSource().sendSuccess(() -> Component.literal("Đã reset cốt truyện Elden Ring về Chương 1: Limgrave!").withStyle(ChatFormatting.YELLOW), true);
                    return 1;
                }))
        );
    }
}
