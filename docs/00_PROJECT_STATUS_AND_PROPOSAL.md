# Báo Cáo Tiến Độ & Bản Đề Xuất Tổng Thể — Elden Ring Core (`eldenring_core`)

> **Nhánh xem thử tích hợp toàn bộ:** `test/full-integration`  
> **Nền tảng kỹ thuật:** Minecraft `1.20.1` • Forge `47.4.4` • GeckoLib `4.4.9` • Epic Fight `20.15.2` (bắt buộc) • Mixin `0.8.5`

Tài liệu này tổng hợp **toàn bộ những gì đang làm / đã tích hợp xong** trên nhánh `test/full-integration` cùng **các hạng mục đề xuất cần team thống nhất** trước khi merge vào `main`.

---

## 1. Tổng Quan 5 Phân Hệ Đã Tích Hợp Chạy Chung

| Phân hệ | Nhánh nguồn | Trạng thái trên `test/full-integration` | Chi tiết nổi bật |
| :--- | :--- | :--- | :--- |
| **1. Boss AI & Model 3D** | `part/boss-ai` + `part/art-models` | ✅ **Đã xong 13/13 Boss** | Đủ **13 Demigod Boss** (Wave 1–4): *Margit, Tree Sentinel, Godrick, Rennala, Radahn, Morgott, Rykard, Mohg, Fire Giant, Malenia, Maliketh, Godfrey, Radagon*. 13 model GeckoLib riêng, chuyển Phase 2 chuẩn 2 thanh máu / đổi texture (*Radagon → Elden Beast*, *Malenia Goddess of Rot*), đồng bộ animation client/server, miễn sát thương rơi & không despawn. |
| **2. Vũ khí 3D & Chiến đấu** | `part/weapons-combat` *(Pe)* | ✅ **Đã tích hợp đủ 10 vũ khí** | 10 vũ khí 3D custom + 26 texture + **10 file Epic Fight capability** (`capabilities/weapons/*.json`), quản lý FP (`FocusPointsManager`), Ash of War (Corpse Piler 3 tầng, Transient Moonlight ngang/dọc...), nâng cấp Smithing `+25` / Somber `+10`, Equip Load. |
| **3. Cơ chế chiến đấu cốt lõi** | `part/combat-mechanics` *(whiteTee2366)* | 🔄 **Đang triển khai trên Epic Fight** | Đã đồng bộ nâng Forge `47.4.4` + gắn **Epic Fight `20.15.2`** (Roll, Stamina, Guard, Battle Mode `R`, combo animation). Bước tiếp theo: nối i-frames né đòn khớp hitbox Boss, Riposte khi Boss `STAGGERED`, Guard Counter phá Poise Boss. |
| **4. Cơ chế Map RPG** | `part/map-mechanics` *(whiteTee2366)* | ✅ **Đã tích hợp** | Hệ thống **Runes** lưu bền vững + rớt Death Marker khi chết, **Cerulean Flask** dùng chung NBT FP với nhánh vũ khí, **Site of Grace** mở GUI Fast Travel, **Fog Gate** liên kết đấu trường Boss. |
| **5. Thế giới 7 Vùng & Hầm ngục** | Đề xuất mới trên `test/full-integration` | ✅ **Đã dựng & PASS QA 100%** | Dimension riêng `eldenring_core:lands_between` cỡ **~`4.800 × 5.600` block** dùng **27 biome Terralith** (7 vùng chính + 14 biome phụ + 6 hang động ngầm 3D), tích hợp **13 mod công trình & hầm ngục Boss miễn phí** (xem chi tiết tại [docs/08_MAP_PROPOSAL.md](08_MAP_PROPOSAL.md)). |

---

## 2. Chi Tiết Đề Xuất Map 7 Vùng & Hầm Ngục Boss (Cần Team Duyệt)

### A. Lý do chuyển từ map tự sinh / Tectonic sang `Terralith 2.5.4`
- Map void phẳng ban đầu hơi đơn điệu; còn **Tectonic** tạo quá nhiều vách đá dựng đứng gây lộn xộn và làm công trình bay lơ lửng.
- **Giải pháp đang áp dụng:** Dùng **27 biome có sẵn của Terralith** (`0 Mixin`, `0 block rác`, tương thích sẵn với mọi mod cấu trúc) chia thành **7 vùng địa lý Elden Ring**:
  1. **Limgrave** `(-500, 1200)` — `highlands` + *Mistwood* (`forested_highlands`) + *Stormhill* (`blooming_plateau`) + vách trắng ven biển + hang pha lê *Siofra* (`cave/crystal_caves`).
  2. **Liurnia** `(-1300, -250)` — `moonlight_valley` + đầm lầy `orchid_swamp` + rừng `moonlight_grove` + hang `crystal_caves`.
  3. **Caelid** `(1150, 950)` — `scarlet_mountains` + `ashen_savanna` + `red_oasis` + hang nấm `fungal_caves` *(đứng dưới nước dính Scarlet Rot!)*.
  4. **Altus Plateau / Leyndell** `(100, -1150)` — `yellowstone` vàng rực + `birch_taiga` + `haze_mountain` + hang `underground_jungle`.
  5. **Mt. Gelmir** `(-1300, -1600)` — `volcanic_crater` + `volcanic_peaks` + `basalt_cliffs` + hang dung nham `mantle_caves` *(quái kháng lửa!)*.
  6. **Mountaintops** `(1400, -1750)` — `wintry_lowlands` + `frozen_cliffs` + `ice_marsh` + hang băng-lửa `frostfire_caves`.
  7. **Miquella's Haligtree** `(0, -2450)` — `sakura_grove` + `amethyst_rainforest` + `snowy_cherry_grove` + hang `underground_jungle`.

### B. Tăng độ đa dạng Boss & Hầm ngục ngoài trời (Phong cách DawnCraft)
Ngoài **13 Demigod Boss** tự làm của team, bản test đã cài thêm **5 mod Boss Dungeon miễn phí** và map đúng vùng qua **69 file Biome Tag**:
- **L_Ender's Cataclysm** *(8 Mega Boss Dungeon: Frosted Prison, Cursed Pyramid, Soul Blacksmith, Burning Arena, Ancient Factory, Acropolis, Ruined Citadel, Sunken City)*
- **Mowzie's Mobs** *(Field Boss: Ferrous Wroughtnaut, Frostmaw, Umvuthi, Sculptor)*
- **Bosses of Mass Destruction** *(Night Lich Tower, Void Blossom, Obsidilith, Nether Gauntlet)*
- **The Graveyard** *(Lich Prison, Crypt/Catacombs, Nghĩa địa)*
- **Born in Chaos** *(Mini-boss & quái đêm Dark Fantasy tuần tra ngoài map)*
- Kết hợp công trình từ **When Dungeons Arise**, **Structory + Towers**, **Explorify**, và **YUNG's Better Dungeons / Strongholds / Mineshafts** *(đầy đủ link Modrinth xem tại [docs/08_MAP_PROPOSAL.md](08_MAP_PROPOSAL.md))*.

---

## 3. Các Hạng Mục Đề Xuất Làm Tiếp (Cần Team Chốt Phương Án)

1. **Structure riêng cho 13 Demigod Boss của team:**
   - **Phương án A (Khuyên dùng):** Cắt vỏ kiến trúc từ các map Elden Ring miễn phí trên Planet Minecraft (*Stormveil của Kalret / Lerfing*, *Raya Lucaria của Ryuunin*, *Nokstella*, *Ranni's Rise*, *Gravesite Plain*) thành file `.nbt` → cho Minecraft tự sinh 13 công trình Boss cố định trên 7 vùng, gắn sẵn **Site of Grace + Fog Gate + Boss**.
   - **Phương án B (Nhanh hơn):** Gắn thẳng 13 Demigod vào phòng trùm cuối của các Legacy Dungeon sẵn có (*When Dungeons Arise / Cataclysm*).
2. **Biểu tượng định hướng từ xa:** Dựng cố định **Cây Erdtree vàng khổng lồ phát sáng (`Y = 315`)** ở tâm Altus Plateau để đứng ở bất kỳ vùng nào nhìn lên cũng thấy.
3. **Di chuyển trên map rộng:** Làm vật phẩm **Còi gọi ngựa Torrent (`Spectral Steed Whistle`)** chạy nhanh + nhảy đôi (Double Jump) để khám phá lục địa `4.800 × 5.600`.
4. **Loot Table chuẩn Elden Ring:** Can thiệp rương trong các hầm mỏ/hầm mộ (*YUNG's, Graveyard, Dungeons Arise*) để rơi **Smithing Stone, Somber Smithing Stone, Golden Seed, Runes** thay vì đồ Vanilla.
5. **Roundtable Hold (Khu an toàn):** Phòng riêng dịch chuyển từ Site of Grace có Thợ rèn nâng cấp vũ khí + Thương nhân tiêu Runes.
6. **Cân bằng độ nặng (RAM/FPS):** Hiện bản test chạy 24 mod (cần `-Xmx4G`) — team test thực tế xem có cần lược bớt mod Boss phụ nào không (ví dụ giữ `Cataclysm + Mowzie + Graveyard`, cân nhắc `Born in Chaos`).

---

## 4. Kết Quả Kiểm Thử Tự Động (Automated QA Suite)

Đã bổ sung bộ test tự động không cần mở game (`scripts/run_full_qa_test.py` / `test_all.bat`) chạy **Forge 47.4.4 Dedicated Server thật + RCON**:
- ✅ **Stage 1 (Static Audit):** 207 file JSON hợp lệ, đủ 13/13 asset Boss GeckoLib, đủ 10 Epic Fight weapon JSON, 0 trùng `modId`.
- ✅ **Stage 2 (Live Server + RCON):** Khởi động sạch tất cả mod trong ~40s • **7/7 vùng Terralith PASS** • **13/13 Boss summon & tick PASS** • **12/12 hầm ngục Boss PASS**.
- ✅ **Stage 3 (Log Scan):** **0 Critical Error, 0 Warning**.

---

## 5. Hướng Dẫn Chạy Thử Cho Thành Viên Team

```bash
git fetch origin
git checkout test/full-integration
# Không cần tải tay: play_test.bat chạy runClient -Pmodpack, Gradle tự tải đúng phiên bản trong modpack/mods.json
play_test.bat                         # Mở game test (dùng JDK 17)
```

- Tạo **New World mới** *(Creative + Cheats ON)* → gõ `/eldenstory enter_world`.
- Nhấn **`M`** xem bản đồ toàn cảnh (**Xaero's World Map**), nhấn **`R`** bật **Epic Fight Battle Mode**.
- Gõ `test_all.bat` nếu muốn chạy lại bài kiểm tra xung đột tự động sau khi sửa code/thêm mod.
