package com.eldenringcore.mechanic;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * Authentic 7-Chapter Elden Ring Storyline & Great Rune Progression Tracker (persisted per world).
 * Route mirrors the original game:
 * Ch1 Limgrave (Margit) -> Ch2 Stormveil (Godrick) -> Ch3 Liurnia & Caelid (2 Great Runes to unseal Leyndell)
 * -> Ch4 Leyndell Royal Capital (Morgott) -> Ch5 Mountaintops (Fire Giant & Burn Erdtree)
 * -> Ch6 Crumbling Farum Azula (Maliketh + optional Haligtree Malenia) -> Ch7 Ashen Capital (Godfrey -> Radagon/Elden Beast).
 */
public class StoryProgressionManager extends SavedData {
    private static final String DATA_NAME = "eldenring_core_story";
    private static final Set<String> GREAT_RUNE_BEARERS = Set.of(
            "godrick", "rennala", "radahn", "morgott", "rykard", "mohg", "malenia"
    );

    private final Set<String> defeatedBosses = new HashSet<>();

    public static StoryProgressionManager get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                StoryProgressionManager::load,
                StoryProgressionManager::new,
                DATA_NAME
        );
    }

    public static StoryProgressionManager load(CompoundTag tag) {
        StoryProgressionManager mgr = new StoryProgressionManager();
        ListTag list = tag.getList("DefeatedBosses", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            mgr.defeatedBosses.add(list.getString(i));
        }
        return mgr;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String id : this.defeatedBosses) {
            list.add(StringTag.valueOf(id));
        }
        tag.put("DefeatedBosses", list);
        return tag;
    }

    public void reset() {
        this.defeatedBosses.clear();
        this.setDirty();
    }

    public boolean isDefeated(String bossId) {
        return this.defeatedBosses.contains(bossId);
    }

    public int getGreatRuneCount() {
        int count = 0;
        for (String bearer : GREAT_RUNE_BEARERS) {
            if (this.defeatedBosses.contains(bearer)) count++;
        }
        return count;
    }

    public int getCurrentChapter() {
        if (isDefeated("radagon")) return 8; // Elden Lord!
        if (isDefeated("maliketh")) return 7; // Ashen Capital -> Godfrey -> Radagon/Elden Beast
        if (isDefeated("fire_giant")) return 6; // Crumbling Farum Azula -> Maliketh
        if (isDefeated("morgott")) return 5; // Mountaintops -> Fire Giant
        if (getGreatRuneCount() >= 2) return 4; // Leyndell Seal Broken -> Morgott
        if (isDefeated("godrick")) return 3; // Liurnia & Caelid -> Collect 2nd Great Rune
        if (isDefeated("margit")) return 2; // Stormveil Castle -> Godrick
        return 1; // Limgrave -> Stormveil Bridge (Margit)
    }

    public String getChapterTitle(int ch) {
        return switch (ch) {
            case 1 -> "Chương 1: Limgrave — Bước Chân Đầu Tiên (The First Step)";
            case 2 -> "Chương 2: Lâu Đài Stormveil — Cổng Sương Vàng";
            case 3 -> "Chương 3: Liurnia & Caelid — Tìm Đủ 2 Great Runes";
            case 4 -> "Chương 4: Kinh Thành Leyndell — Dưới Gốc Cây Erdtree";
            case 5 -> "Chương 5: Đỉnh Núi Người Khổng Lồ — Đốt Cây Erdtree";
            case 6 -> "Chương 6: Crumbling Farum Azula — Giải Phóng Destined Death";
            case 7 -> "Chương 7: Thủ Đô Tro Tàn & Lõi Erdtree — Trận Chiến Cuối";
            default -> "KẾT THÚC: BẠN ĐÃ LÊN NGÔI ELDEN LORD!";
        };
    }

    public String getCurrentObjective() {
        return switch (getCurrentChapter()) {
            case 1 -> "Đi theo tia sáng Grace từ The First Step → Church of Elleh → vượt Cầu Stormveil đánh bại Margit, the Fell Omen (tùy chọn: thử sức với Tree Sentinel).";
            case 2 -> "Tiến sâu vào Lâu đài Stormveil, hạ gục Godrick the Grafted để đoạt Great Rune đầu tiên.";
            case 3 -> "Thu thập đủ 2 Great Runes (hiện có " + getGreatRuneCount() + "/2): đánh bại Rennala (Liurnia) hoặc Starscourge Radahn (Caelid) để phá phong ấn Leyndell.";
            case 4 -> "Phong ấn Leyndell đã mở! Tiến vào Kinh thành Hoàng kim hạ Morgott, the Omen King (nhánh phụ: Rykard, Mohg).";
            case 5 -> "Gai Erdtree chặn lối! Tiến lên phương Bắc hạ Fire Giant để đốt cây Erdtree tại Forge of the Giants.";
            case 6 -> "Erdtree bốc cháy! Tại Crumbling Farum Azula, hạ Beast Clergyman / Maliketh (nhánh bí mật: thách đấu Malenia tại Haligtree).";
            case 7 -> "Trở về Thủ đô Tro tàn, hạ Godfrey / Hoarah Loux rồi bước vào lõi Erdtree đánh Radagon & Elden Beast!";
            default -> "Tất cả Đại Boss cốt truyện chính đã bị khuất phục. Bạn là Elden Lord của The Lands Between!";
        };
    }

    public void recordBossDefeated(ServerLevel level, String bossId) {
        int prevChapter = getCurrentChapter();
        boolean newlyDefeated = this.defeatedBosses.add(bossId);
        if (!newlyDefeated) return;
        this.setDirty();

        int newChapter = getCurrentChapter();
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            if (GREAT_RUNE_BEARERS.contains(bossId)) {
                p.sendSystemMessage(Component.literal("[Elden Ring] Đã đoạt Great Rune của " + bossId.toUpperCase()
                        + "! (Tổng Great Runes: " + getGreatRuneCount() + ")").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }
            if (newChapter > prevChapter) {
                p.sendSystemMessage(Component.literal("=========================================").withStyle(ChatFormatting.YELLOW));
                p.sendSystemMessage(Component.literal(getChapterTitle(newChapter)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                p.sendSystemMessage(Component.literal("Mục tiêu: " + getCurrentObjective()).withStyle(ChatFormatting.WHITE));
                p.sendSystemMessage(Component.literal("=========================================").withStyle(ChatFormatting.YELLOW));
            }
        }
    }
}
