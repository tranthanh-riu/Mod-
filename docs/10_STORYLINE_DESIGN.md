# Thiết Kế Cốt Truyện Chính & Hệ Thống Quest Phụ — Elden Ring Core

> Tài liệu thiết kế tuyến cốt truyện **7 Chương Chính + 3 Nhánh Phụ Biểu Tượng + Bảng Săn Boss Ngoài** kết nối trực tiếp 13 Demigod Boss, 10 Vũ khí 3D (`part/weapons-combat`), Site of Grace / Runes (`part/map-mechanics`), Epic Fight (`part/combat-mechanics`) và bộ 25 mod trong Modpack.

---

## 1. Mạch Cốt Truyện Chính (7 Chương — Đã có khung trong `StoryProgressionManager`)

| Chương | Vùng | Boss Chính | Điều kiện mở / Mốc tiến độ |
| :--- | :--- | :--- | :--- |
| **Ch. 1** | **Limgrave** | **Margit, the Fell Omen** *(tùy chọn: Tree Sentinel)* | Khởi đầu tại *The First Step*, gặp **Varré / Melina**, nhận Còi gọi ngựa **Torrent** & mở **Roundtable Hold**. |
| **Ch. 2** | **Lâu đài Stormveil** | **Godrick the Grafted** | Hạ Margit để mở đường vào thành → hạ Godrick đoạt **Great Rune #1**. |
| **Ch. 3** | **Liurnia / Caelid / Mt. Gelmir** *(tự do chọn)* | **Rennala** hoặc **Starscourge Radahn** *(hoặc Rykard / Mohg)* | Thu thập đủ **2 Great Runes** bất kỳ để phá phong ấn vàng vào kinh thành **Leyndell**. |
| **Ch. 4** | **Altus Plateau / Leyndell** | **Morgott, the Omen King** | Phá phong ấn Leyndell → tiến tới chân **Cây Erdtree** hạ Morgott → nhận **Rold Medallion** mở lối lên phương Bắc. |
| **Ch. 5** | **Mountaintops of the Giants** | **Fire Giant** | Vượt núi tuyết hạ Fire Giant → kích hoạt sự kiện **Đốt Cây Erdtree** tại Chảo Lửa. |
| **Ch. 6** | **Crumbling Farum Azula** | **Beast Clergyman / Maliketh** | Cây Erdtree bốc cháy, dịch chuyển lên đền bay hạ Maliketh giải phóng *Destined Death*. |
| **Ch. 7** | **Thủ Đô Tro Tàn & Lõi Erdtree** | **Godfrey / Hoarah Loux** → **Radagon & Elden Beast** | Trận chiến cuối cùng → hoàn thành game, lên ngôi **Elden Lord**! |

---

## 2. Kết Nối Cốt Truyện ↔ 10 Vũ Khí 3D (`Remembrance` — Ký Ức Boss)

Thay vì chế tạo hay nhặt dễ dàng ngoài đường, các vũ khí huyền thoại gắn trực tiếp với tiến độ hạ Boss (đổi tại **Roundtable Hold** hoặc Site of Grace):

- **Tree Sentinel** → `Golden Halberd`
- **Bloodhound / Boss phụ Limgrave** → `Bloodhound's Fang`
- **Rennala / Nhánh Liurnia** → `Moonveil`
- **Starscourge Radahn** → `Starscourge Greatsword`
- **Nhánh Ranni (Liurnia → Radahn → Thành phố ngầm)** → `Dark Moon Greatsword`
- **Rykard** → `Blasphemous Blade`
- **Mohg / Nhánh Máu** → `Rivers of Blood`
- **Leyndell / Altus** → `Bolt of Gransax`
- **Maliketh** → `Maliketh's Black Blade`
- **Malenia (Vùng bí mật Haligtree)** → `Hand of Malenia`

---

## 3. Hệ Thống Quest Phụ Gọn (Ít code, không lỗi Multiplayer)

> **Nguyên tắc:** Không làm quest hội thoại chạy đi chạy lại giữa 20 NPC. Tập trung vào **3 nhóm Quest phụ gọn** để gắn toàn bộ 25 mod thành vòng lặp: *Đi hầm ngục / Săn boss phụ → Lấy Đá rèn & Runes → Nâng vũ khí → Hạ Demigod mở chương mới*.

### Nhóm 1 — Bảng Săn Boss Ngoài & Hầm Ngục *(Kết nối 5 mod Boss DawnCraft)*
- **Tier I–II (Limgrave & Liurnia):**
  - [ ] Hạ **Ferrous Wroughtnaut** *(Mowzie)* / **Void Blossom** *(BOMD)* → thưởng **Smithing Stone (+1 → +3)** + Runes.
  - [ ] Hạ **Night Lich** *(BOMD)* / **Corrupted Champion** *(The Graveyard)* → thưởng nguyên liệu nâng **Cerulean Flask / FP**.
- **Tier III–IV (Caelid, Altus, Mt. Gelmir):**
  - [ ] Hạ **Ancient Remnant** / **The Harbinger** / **Netherite Monstrosity** *(Cataclysm)* → thưởng **Somber Smithing Stone (+5 → +7)** + Runes lớn.
- **Tier V–VI (Mountaintops & Haligtree):**
  - [ ] Hạ **Maledictus** / **Ignis** / **Ender Guardian** *(Cataclysm)* & **Frostmaw** *(Mowzie)* → thưởng **Somber Smithing Stone (+8 → +10)** chuẩn bị trận cuối.

### Nhóm 2 — 3 Nhánh Phụ Biểu Tượng (Mỗi nhánh 2–3 bước ngắn)
1. **Nhánh Ranni (Age of Stars):**
   - Gặp Ranni tại Tháp Liurnia → hạ **Radahn** ở Caelid → khám phá thành phố ngầm lấy cổ vật → nhận **`Dark Moon Greatsword`**.
2. **Nhánh Haligtree (Mở khóa Vùng bí mật #7):**
   - Tìm đủ **2 mảnh Haligtree Secret Medallion** (rớt từ 2 pháo đài/boss phụ ở Liurnia & Mountaintops) → mở cổng vào **Haligtree** thách đấu **Malenia**.
3. **Nhánh Lửa & Máu (Volcano Manor / Mohgwyn):**
   - Săn 2 mục tiêu ngoài đồng → mở đường đánh **Rykard** (Mt. Gelmir) & **Mohg** (Caelid/Ngầm).

### Nhóm 3 — Cột Mốc Khám Phá (Exploration Milestones)
- Khám phá đủ 7 vùng & kích hoạt Site of Grace.
- Thu thập **Golden Seed** (tăng số lượng bình Flask) từ các nhà thờ/cây vàng nhỏ.
- Chinh phục hầm mỏ (**Mineshafts**) tìm **Smithing Stones** & quái đêm (**Born in Chaos**) lấy nguyên liệu hiếm.

---

## 4. NPC Cốt Lõi & Công Cụ Dẫn Đường

### 5 NPC tối giản (chỉ cần hội thoại ngắn + giao dịch/nâng cấp)
1. **Varré** *(Limgrave):* Hướng dẫn nhập môn (Grace, Runes, Epic Fight) & mở lối nhánh Mohg.
2. **Melina** *(Site of Grace đầu tiên):* Trao còi gọi ngựa **Torrent** & mở dịch chuyển về **Roundtable Hold**.
3. **Thợ rèn Hewg** *(Roundtable Hold):* Nâng cấp vũ khí `+1 → +25 / +10` bằng Runes + Smithing Stone.
4. **Gideon Ofnir** *(Roundtable Hold):* Chỉ hướng 7 Chủ nhân Great Rune & thưởng khi báo tin hạ từng Boss.
5. **Ranni** *(Liurnia):* Mở nhánh phụ `Dark Moon Greatsword`.

### Công cụ dẫn đường & Giao diện Quest
- **Tia Sáng Vàng (`Guidance of Grace`):** Hạt sáng vàng tại Site of Grace chỉ về hướng Boss chính của Chương hiện tại.
- **Sổ Nhiệm Vụ (Đề xuất):** Cài thêm **FTB Quests** vào Modpack (miễn phí, hỗ trợ chơi nhóm chia team, không cần code GUI phức tạp, quản lý toàn bộ 7 Chương + 3 Nhánh Phụ + Bảng Săn Boss Ngoài bằng file cấu hình trong `modpack/overrides/`).
- **Rương kho báu nhiều người:** Đã tích hợp **Lootr** trong `modpack/mods.json` để cả nhóm mở chung rương thưởng không tranh nhau.
