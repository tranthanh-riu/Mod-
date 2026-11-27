# Đề Xuất Kiến Trúc Map & Worldgen — The Lands Between (`test/full-integration`)

Tài liệu tóm tắt hướng thiết kế **Map 7 Vùng Lands Between** hiện đang chạy thử nghiệm trên nhánh `test/full-integration` để team xem xét và góp ý trước khi chốt merge.

---

## 1. Kiến trúc tổng thể (Forge `47.4.4` + Epic Fight `20.15.2` + GeckoLib `4.4.9`)

- **Dimension riêng:** `eldenring_core:lands_between` (địa hình tự nhiên chuẩn Minecraft/Terralith, hang sâu `Y = -64..320`).
- **Quy mô lục địa:** **~`4.800 × 5.600` block** (mỗi vùng rộng `1.400 – 2.000` block), bao quanh bởi vách đá biển trắng (`white_cliffs`) và biển sâu (`deep_ocean`).
- **Xử lý chống đơ `/locate` & mắt dẫn đường:** Lục địa chính giữ nguyên 1:1 trong `[-4096, +4096]`, ngoài biển xa lặp quần đảo qua viền đại dương (không vết cắt) để `/locate structure`, mắt Cataclysm và Soul Star không bao giờ quét biển vô tận gây lag.
- **RPG Scaling theo vùng (`RegionDifficultyManager`):** Hiện Title Banner khi sang vùng mới (không spam khi đi qua biome phụ trong cùng vùng), tăng Máu/Sát thương/Giáp quái từ **Tier I → Tier VI**, quái Mt. Gelmir kháng lửa, đứng dưới nước Caelid tích **Scarlet Rot**.

---

## 2. Quy hoạch 27 Biome từ `Terralith 2.5.4` (7 Vùng Chính + 14 Biome Phụ + 6 Hang Ngầm 3D)

| Vùng Elden Ring | Tọa độ Grace | Biome Chính | Biome Phụ (Xen kẽ tự nhiên) | Hang Ngầm (`Y < 32`) |
| :--- | :--- | :--- | :--- | :--- |
| **1. Limgrave** *(Tier I)* | `(-500, 1200)` | `terralith:highlands` | `forested_highlands` *(Mistwood)*, `blooming_plateau` *(Stormhill)*, `white_cliffs` *(Bờ biển)* | `cave/crystal_caves` *(Siofra River)* |
| **2. Liurnia** *(Tier II)* | `(-1300, -250)` | `terralith:moonlight_valley` | `orchid_swamp` *(Đầm Raya Lucaria)*, `moonlight_grove` *(Caria)* | `cave/crystal_caves` *(Ainsel/Nokstella)* |
| **3. Caelid** *(Tier III)* | `(1150, 950)` | `terralith:scarlet_mountains` | `ashen_savanna` *(Rừng khô cháy)*, `red_oasis` *(Swamp of Aeonia)* | `cave/fungal_caves` *(Lake of Rot)* |
| **4. Altus / Leyndell** *(Tier IV)* | `(100, -1150)` | `terralith:yellowstone` | `birch_taiga` *(Rừng vàng)*, `haze_mountain` *(Đồi mù)* | `cave/underground_jungle` *(Deeproot)* |
| **5. Mt. Gelmir** *(Tier IV)* | `(-1300, -1600)` | `terralith:volcanic_crater` | `volcanic_peaks`, `basalt_cliffs` | `cave/mantle_caves` *(Lò dung nham)* |
| **6. Mountaintops** *(Tier V)* | `(1400, -1750)` | `terralith:wintry_lowlands` | `frozen_cliffs`, `ice_marsh` *(Hồ băng)* | `cave/frostfire_caves` *(Băng–Lửa)* |
| **7. Haligtree** *(Tier VI)* | `(0, -2450)` | `terralith:sakura_grove` | `amethyst_rainforest`, `snowy_cherry_grove` *(Ordina)* | `cave/underground_jungle` *(Rễ Haligtree)* |

---

## 3. Bộ 13 Mod Biome, Công Trình & Boss Ngoài (100% miễn phí trên Modrinth)

Đã map **69 file Biome-Structure Tag** + tối ưu **12 `structure_set`** để mỗi vùng sinh đúng công trình & boss hợp lore:

- **Biome:** [Terralith](https://modrinth.com/mod/terralith)
- **Lâu đài & Công trình:**
  - [When Dungeons Arise](https://modrinth.com/mod/when-dungeons-arise) *(Shiraz Palace, Keep Kayra, Monastery, Thornborn Towers, Coliseum, Foundry, Plague Asylum...)*
  - [Structory](https://modrinth.com/mod/structory) & [Structory: Towers](https://modrinth.com/mod/structory-towers) *(Chapel/Church of Marika, Old Manor, Northern Ruin, hệ thống Tháp)*
  - [Explorify](https://modrinth.com/mod/explorify) *(Mausoleum, Black Spiral, Watchtowers, Campsites, Ruins)*
- **Hầm ngục & Mỏ Đá Rèn:**
  - [YUNG's Better Dungeons](https://modrinth.com/mod/yungs-better-dungeons) *(Catacombs)*
  - [YUNG's Better Strongholds](https://modrinth.com/mod/yungs-better-strongholds) *(Hero's Graves / Shunning-Grounds)*
  - [YUNG's Better Mineshafts](https://modrinth.com/mod/yungs-better-mineshafts) *(Smithing Stone Tunnels)*
- **Hầm ngục Boss & Field Boss phong cách DawnCraft:**
  - [L_Ender's Cataclysm](https://modrinth.com/mod/l_enders-cataclysm) *(Frosted Prison, Cursed Pyramid, Soul Blacksmith, Burning Arena, Ancient Factory, Acropolis, Ruined Citadel, Sunken City)*
  - [Mowzie's Mobs](https://modrinth.com/mod/mowzies-mobs) *(Ferrous Wroughtnaut, Frostmaw, Umvuthi, Sculptor)*
  - [Bosses of Mass Destruction](https://modrinth.com/mod/bosses-of-mass-destruction-forge) *(Night Lich Tower, Void Blossom, Obsidilith, Nether Gauntlet)*
  - [The Graveyard](https://modrinth.com/mod/the-graveyard-forge) *(Lich Prison, Crypts, Nghĩa địa)*
  - [Born in Chaos](https://modrinth.com/mod/borninchaos) *(Mini-boss & quái đêm Dark Fantasy)*

---

## 4. Hướng gắn Structure riêng cho 13 Demigod Boss của team (Đang mở thảo luận)

Các bản build Elden Ring miễn phí (block vanilla) tìm được trên Planet Minecraft có thể cắt `.schem` / `.nbt` làm phần **vỏ công trình**, rồi gắn **Fog Gate + Site of Grace + 13 Demigod Boss** của `eldenring_core`:

- **Project Elden Block (Free Limgrave — Church of Elleh, Stormveil):** https://www.planetminecraft.com/project/elden-ring-full-map-recreated-in-minecraft/
- **Stormveil Castle + Limgrave (Kalret):** https://www.planetminecraft.com/project/building-elden-ring-in-minecraft/
- **Raya Lucaria Academy (Ryuunin):** https://www.planetminecraft.com/project/raya-lucaria-academy-inspired-build/
- **Nokstella, Eternal City (Lobsterrr):** https://www.planetminecraft.com/project/nokstella-eternal-city-elden-ring/
- **Ranni's Rise (Schematic):** https://www.planetminecraft.com/project/elden-ring-ranni-s-rise/
- **Gravesite Plain (Schematic):** https://www.planetminecraft.com/project/elden-ring-gravesite-plain/

### Điểm cần team góp ý thêm:
1. Có giữ đủ cả 5 mod Boss ngoài kiểu DawnCraft không, hay lọc gọn bớt cho nhẹ RAM?
2. 13 Demigod Boss của team: làm **13 structure `.nbt` riêng** (từ schematic free + tự build), hay **gắn vào cuối các Legacy Dungeon sẵn có**?
3. Có thêm **Cây Erdtree khổng lồ phát sáng ở Altus**, **Còi gọi ngựa Torrent**, và **chỉnh Loot Table rương hầm ngục rơi Smithing Stone / Golden Seed / Runes** không?

---

## 5. Cách kéo nhánh này về chạy thử

```bash
git fetch origin
git checkout test/full-integration
# Tải 22 mod miễn phí (Terralith, Cataclysm, Mowzie, Dungeons Arise...) vào libs_dev/
python scripts/download_dev_mods.py
# Chạy game (JDK 17)
play_test.bat
```

Vào game tạo **New World** (Creative + Cheats ON), gõ `/eldenstory enter_world` rồi nhấn **`M`** (Xaero's World Map) và **`R`** (Epic Fight Battle Mode) để test!  
Muốn chạy kiểm tra tự động không cần mở game (7 biome + 13 boss + 12 hầm ngục + quét log lỗi): chạy `test_all.bat`.
