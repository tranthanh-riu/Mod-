package com.eldenringcore.mechanic;

import com.eldenringcore.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Generates an authentic, immediately playable Elden Ring Chapter 1-2 route:
 * The First Step (Site of Grace) -> Guidance of Grace Road -> Church of Elleh (Anvil + Tree Sentinel Arena)
 * -> Stormveil Stone Bridge (Fog Gate + Margit Arena) -> Stormveil Castle Courtyard (Fog Gate + Godrick Arena)
 * -> Colossal Glowing Golden Erdtree towering on the horizon!
 */
public class LandsBetweenBuilder {

    public static void buildLimgraveToStormveilRoute(ServerLevel level, BlockPos origin) {
        int baseY = origin.getY() - 1;
        int ox = origin.getX();
        int oz = origin.getZ();

        // 1. THE FIRST STEP (Z: -4 .. +6)
        fillBox(level, ox - 6, baseY, oz - 4, ox + 6, baseY, oz + 6, Blocks.STONE_BRICKS);
        placePillar(level, ox - 5, baseY + 1, oz - 3, 6);
        placePillar(level, ox + 5, baseY + 1, oz - 3, 5);
        level.setBlockAndUpdate(new BlockPos(ox, baseY + 1, oz + 2), ModBlocks.SITE_OF_GRACE.get().defaultBlockState());

        // Golden Guidance of Grace road leading North (+Z) to Church of Elleh
        for (int z = 7; z <= 54; z++) {
            fillBox(level, ox - 3, baseY, oz + z, ox + 3, baseY, oz + z, Blocks.COBBLESTONE);
            if (z % 8 == 0) {
                level.setBlockAndUpdate(new BlockPos(ox, baseY, oz + z), Blocks.GLOWSTONE.defaultBlockState());
            }
        }

        // 2. CHURCH OF ELLEH (Z: 22 .. 34) + TREE SENTINEL MEADOW (X: +18)
        fillBox(level, ox - 8, baseY, oz + 22, ox + 8, baseY, oz + 34, Blocks.MOSSY_STONE_BRICKS);
        placePillar(level, ox - 7, baseY + 1, oz + 23, 7);
        placePillar(level, ox + 7, baseY + 1, oz + 23, 7);
        placePillar(level, ox - 7, baseY + 1, oz + 33, 6);
        placePillar(level, ox + 7, baseY + 1, oz + 33, 6);
        level.setBlockAndUpdate(new BlockPos(ox - 3, baseY + 1, oz + 28), Blocks.ANVIL.defaultBlockState()); // Smithing Table
        level.setBlockAndUpdate(new BlockPos(ox + 3, baseY + 1, oz + 28), Blocks.CAMPFIRE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(ox, baseY + 1, oz + 28), ModBlocks.SITE_OF_GRACE.get().defaultBlockState());

        // Tree Sentinel Open-Field Arena (East of road)
        fillBox(level, ox + 12, baseY, oz + 18, ox + 28, baseY, oz + 34, Blocks.GRASS_BLOCK);
        placeBossArena(level, new BlockPos(ox + 20, baseY, oz + 26), "eldenring_core:tree_sentinel");

        // 3. STORMVEIL BRIDGE — MARGIT, THE FELL OMEN ARENA (Z: 55 .. 92)
        fillBox(level, ox - 6, baseY, oz + 55, ox + 6, baseY, oz + 92, Blocks.DEEPSLATE_BRICKS);
        for (int z = 55; z <= 92; z++) {
            level.setBlockAndUpdate(new BlockPos(ox - 6, baseY + 1, oz + z), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(ox + 6, baseY + 1, oz + z), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState());
            if (z % 10 == 0) {
                placePillar(level, ox - 6, baseY + 1, oz + z, 5);
                placePillar(level, ox + 6, baseY + 1, oz + z, 5);
            }
        }
        placeFogGateArch(level, ox, baseY + 1, oz + 56);
        placeBossArena(level, new BlockPos(ox, baseY, oz + 74), "eldenring_core:margit");

        // 4. STORMVEIL CASTLE COURTYARD — GODRICK THE GRAFTED ARENA (Z: 98 .. 138)
        fillBox(level, ox - 3, baseY, oz + 93, ox + 3, baseY, oz + 97, Blocks.DEEPSLATE_BRICKS);
        fillBox(level, ox - 16, baseY, oz + 98, ox + 16, baseY, oz + 138, Blocks.STONE_BRICKS);
        for (int x = -16; x <= 16; x++) {
            fillBox(level, ox + x, baseY + 1, oz + 138, ox + x, baseY + 6, oz + 138, Blocks.CRACKED_STONE_BRICKS);
            if (Math.abs(x) > 3) {
                fillBox(level, ox + x, baseY + 1, oz + 98, ox + x, baseY + 6, oz + 98, Blocks.CRACKED_STONE_BRICKS);
            }
        }
        for (int z = 98; z <= 138; z++) {
            fillBox(level, ox - 16, baseY + 1, oz + z, ox - 16, baseY + 6, oz + z, Blocks.CRACKED_STONE_BRICKS);
            fillBox(level, ox + 16, baseY + 1, oz + z, ox + 16, baseY + 6, oz + z, Blocks.CRACKED_STONE_BRICKS);
        }
        placeFogGateArch(level, ox, baseY + 1, oz + 99);
        placeBossArena(level, new BlockPos(ox, baseY, oz + 118), "eldenring_core:godrick");

        // 5. COLOSSAL GLOWING GOLDEN ERDTREE ON THE HORIZON (Z: 175)
        buildGoldenErdtree(level, ox, baseY, oz + 175);

        // Register Chapter 1-2 Graces in Fast Travel
        GraceFastTravelManager graces = GraceFastTravelManager.get(level);
        graces.registerGrace(new BlockPos(ox, baseY + 1, oz + 2), "Ch.1 — The First Step (Limgrave)");
        graces.registerGrace(new BlockPos(ox, baseY + 1, oz + 28), "Ch.1 — Church of Elleh");
    }

    /**
     * Builds all 7 Chapters of the Elden Ring Storyline (all 13 Boss Arenas + Graces + Catacomb)!
     */
    public static void buildAllSevenChapters(ServerLevel level, BlockPos origin) {
        buildLimgraveToStormveilRoute(level, origin);
        int baseY = origin.getY() - 1;
        int ox = origin.getX();
        int oz = origin.getZ();
        GraceFastTravelManager graces = GraceFastTravelManager.get(level);

        // Ch.3 West: Raya Lucaria Academy (Rennala)
        buildThemedArena(level, ox - 80, baseY, oz + 120, Blocks.DARK_PRISMARINE, Blocks.BLUE_STAINED_GLASS, "eldenring_core:rennala");
        graces.registerGrace(new BlockPos(ox - 80, baseY + 1, oz + 104), "Ch.3 — Raya Lucaria Academy (Rennala)");

        // Ch.3 East: Caelid Wailing Dunes (Starscourge Radahn)
        buildThemedArena(level, ox + 80, baseY, oz + 120, Blocks.RED_SANDSTONE, Blocks.NETHERRACK, "eldenring_core:radahn");
        graces.registerGrace(new BlockPos(ox + 80, baseY + 1, oz + 104), "Ch.3 — Caelid Wailing Dunes (Radahn)");

        // Ch.4: Leyndell Royal Capital (Morgott) + Side Shrines (Rykard, Mohg)
        buildThemedArena(level, ox, baseY, oz + 225, Blocks.SMOOTH_SANDSTONE, Blocks.GOLD_BLOCK, "eldenring_core:morgott");
        graces.registerGrace(new BlockPos(ox, baseY + 1, oz + 209), "Ch.4 — Leyndell Royal Capital (Morgott)");

        buildThemedArena(level, ox - 80, baseY, oz + 225, Blocks.MAGMA_BLOCK, Blocks.BLACKSTONE, "eldenring_core:rykard");
        graces.registerGrace(new BlockPos(ox - 80, baseY + 1, oz + 209), "Ch.4 (Phụ) — Volcano Manor (Rykard)");

        buildThemedArena(level, ox + 80, baseY, oz + 225, Blocks.CRIMSON_NYLIUM, Blocks.POLISHED_BLACKSTONE, "eldenring_core:mohg");
        graces.registerGrace(new BlockPos(ox + 80, baseY + 1, oz + 209), "Ch.4 (Phụ) — Mohgwyn Palace (Mohg)");

        // Ch.5: Mountaintops of the Giants (Fire Giant)
        buildThemedArena(level, ox, baseY, oz + 295, Blocks.SNOW_BLOCK, Blocks.PACKED_ICE, "eldenring_core:fire_giant");
        graces.registerGrace(new BlockPos(ox, baseY + 1, oz + 279), "Ch.5 — Forge of the Giants (Fire Giant)");

        // Ch.6: Crumbling Farum Azula (Maliketh) & Secret Haligtree Roots (Malenia)
        buildThemedArena(level, ox - 60, baseY + 20, oz + 365, Blocks.CRACKED_STONE_BRICKS, Blocks.DEEPSLATE_TILES, "eldenring_core:maliketh");
        graces.registerGrace(new BlockPos(ox - 60, baseY + 21, oz + 349), "Ch.6 — Crumbling Farum Azula (Maliketh)");

        buildThemedArena(level, ox + 60, baseY, oz + 365, Blocks.OAK_WOOD, Blocks.CRIMSON_HYPHAE, "eldenring_core:malenia");
        graces.registerGrace(new BlockPos(ox + 60, baseY + 1, oz + 349), "Ch.6 (Bí mật) — Haligtree Roots (Malenia)");

        // Ch.7: Ashen Capital (Godfrey) -> Erdtree Core (Radagon / Elden Beast)
        buildThemedArena(level, ox, baseY, oz + 435, Blocks.GRAY_CONCRETE_POWDER, Blocks.GOLD_BLOCK, "eldenring_core:godfrey");
        graces.registerGrace(new BlockPos(ox, baseY + 1, oz + 419), "Ch.7 — Ashen Capital Throne (Godfrey)");

        buildThemedArena(level, ox, baseY, oz + 505, Blocks.QUARTZ_BLOCK, Blocks.GLOWSTONE, "eldenring_core:radagon");
        graces.registerGrace(new BlockPos(ox, baseY + 1, oz + 489), "Ch.7 (Cuối) — Inside the Erdtree (Radagon/Elden Beast)");
    }

    private static void buildThemedArena(ServerLevel level, int cx, int baseY, int cz, Block floor, Block trim, String bossId) {
        fillBox(level, cx - 14, baseY, cz - 14, cx + 14, baseY, cz + 14, floor);
        for (int i = -14; i <= 14; i++) {
            level.setBlockAndUpdate(new BlockPos(cx + i, baseY + 1, cz + 14), trim.defaultBlockState());
            if (Math.abs(i) > 3) {
                level.setBlockAndUpdate(new BlockPos(cx + i, baseY + 1, cz - 14), trim.defaultBlockState());
            }
            level.setBlockAndUpdate(new BlockPos(cx - 14, baseY + 1, cz + i), trim.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(cx + 14, baseY + 1, cz + i), trim.defaultBlockState());
        }
        placeFogGateArch(level, cx, baseY + 1, cz - 14);
        level.setBlockAndUpdate(new BlockPos(cx, baseY + 1, cz - 16), ModBlocks.SITE_OF_GRACE.get().defaultBlockState());
        placeBossArena(level, new BlockPos(cx, baseY, cz), bossId);
    }

    /**
     * Generates an underground Elden Ring Catacomb dungeon with crypt corridor, pillars & Smithing Stone chest!
     */
    public static void buildCatacomb(ServerLevel level, BlockPos origin) {
        int ox = origin.getX();
        int oy = origin.getY() - 1;
        int oz = origin.getZ() + 4;

        // Stone entrance archway
        fillBox(level, ox - 3, oy, oz, ox + 3, oy + 4, oz + 2, Blocks.MOSSY_STONE_BRICKS);
        fillBox(level, ox - 1, oy + 1, oz, ox + 1, oy + 3, oz + 2, Blocks.AIR);

        // Crypt corridor & underground boss/treasure chamber
        fillBox(level, ox - 6, oy - 6, oz + 3, ox + 6, oy + 1, oz + 24, Blocks.DEEPSLATE_BRICKS);
        fillBox(level, ox - 5, oy - 5, oz + 4, ox + 5, oy, oz + 23, Blocks.AIR);
        // Descending stairs
        for (int i = 0; i < 6; i++) {
            fillBox(level, ox - 1, oy - i, oz + 3 + i, ox + 1, oy - i, oz + 3 + i, Blocks.STONE_BRICK_STAIRS);
            fillBox(level, ox - 1, oy - i + 1, oz + 3 + i, ox + 1, oy - i + 3, oz + 3 + i, Blocks.AIR);
        }
        // Crypt pillars & lanterns
        for (int z = 11; z <= 21; z += 5) {
            fillBox(level, ox - 3, oy - 5, oz + z, ox - 3, oy, oz + z, Blocks.CHISELED_STONE_BRICKS);
            fillBox(level, ox + 3, oy - 5, oz + z, ox + 3, oy, oz + z, Blocks.CHISELED_STONE_BRICKS);
            level.setBlockAndUpdate(new BlockPos(ox - 3, oy - 2, oz + z - 1), Blocks.SOUL_LANTERN.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(ox + 3, oy - 2, oz + z - 1), Blocks.SOUL_LANTERN.defaultBlockState());
        }
        // Reward Chest with Smithing Stones
        BlockPos chestPos = new BlockPos(ox, oy - 5, oz + 21);
        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        if (level.getBlockEntity(chestPos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
            chest.setItem(12, new net.minecraft.world.item.ItemStack(com.eldenringcore.registry.ModItems.SMITHING_STONE.get(), 4));
            chest.setItem(13, new net.minecraft.world.item.ItemStack(com.eldenringcore.registry.ModItems.SOMBER_SMITHING_STONE.get(), 2));
            chest.setItem(14, new net.minecraft.world.item.ItemStack(com.eldenringcore.registry.ModItems.FLASK_OF_CRIMSON_TEARS.get(), 1));
        }
    }

    private static void placeBossArena(ServerLevel level, BlockPos pos, String bossId) {
        level.setBlockAndUpdate(pos, ModBlocks.BOSS_ARENA_CONTROLLER.get().defaultBlockState());
        if (level.getBlockEntity(pos) instanceof BossArenaBlockEntity arena) {
            arena.setBossId(bossId);
        }
    }

    private static void placeFogGateArch(ServerLevel level, int cx, int y, int z) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = 0; dy <= 5; dy++) {
                boolean border = Math.abs(dx) == 3 || dy == 5;
                Block b = border ? Blocks.POLISHED_DEEPSLATE : ModBlocks.FOG_GATE.get();
                level.setBlockAndUpdate(new BlockPos(cx + dx, y + dy, z), b.defaultBlockState());
            }
        }
    }

    private static void placePillar(ServerLevel level, int x, int y, int z, int height) {
        for (int dy = 0; dy < height; dy++) {
            level.setBlockAndUpdate(new BlockPos(x, y + dy, z), Blocks.STONE_BRICKS.defaultBlockState());
        }
        level.setBlockAndUpdate(new BlockPos(x, y + height, z), Blocks.LANTERN.defaultBlockState());
    }

    private static void buildGoldenErdtree(ServerLevel level, int cx, int baseY, int cz) {
        // Massive Golden Trunk (height 42)
        for (int dy = 0; dy <= 42; dy++) {
            int radius = dy < 8 ? 5 : 3;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        Block b = ((dx + dy + dz) % 4 == 0) ? Blocks.GLOWSTONE : Blocks.GOLD_BLOCK;
                        level.setBlockAndUpdate(new BlockPos(cx + dx, baseY + dy, cz + dz), b.defaultBlockState());
                    }
                }
            }
        }
        // Radiant Golden Canopy (height 38..54)
        for (int dy = 38; dy <= 54; dy++) {
            int r = 16 - Math.abs(dy - 45) * 2;
            if (r < 4) r = 4;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int distSq = dx * dx + dz * dz;
                    if (distSq <= r * r && distSq >= (r - 4) * (r - 4) || dy >= 50) {
                        Block b = ((dx + dz + dy) % 3 == 0) ? Blocks.GLOWSTONE : Blocks.YELLOW_STAINED_GLASS;
                        level.setBlockAndUpdate(new BlockPos(cx + dx, baseY + dy, cz + dz), b.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void fillBox(ServerLevel level, int x1, int y1, int z1, int x2, int y2, int z2, Block block) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    level.setBlockAndUpdate(new BlockPos(x, y, z), block.defaultBlockState());
                }
            }
        }
    }
}
