# Thiết Kế 5 Hệ Thống Gameplay RPG Cốt Lõi — Elden Ring Core

> Chốt định hướng 5 hệ thống ngoài Map (tương thích hoàn toàn với **Epic Fight `20.15.2`**, **Curios API**, **13 Demigod Boss**, **10 Vũ khí 3D** và **Co-op Multiplayer**).

---

## 1. Hệ Thống Level Up Bằng Runes Tại Site of Grace *(Chuẩn Elden Ring + Epic Fight)*
- Tiêu **Runes** (từ `part/map-mechanics`) tại Site of Grace để nâng 6 chỉ số gốc (chi phí tăng dần theo cấp):
  - **Vigor (Sinh lực):** Tăng Máu tối đa (`Attributes.MAX_HEALTH`).
  - **Mind (Tinh thần):** Tăng **FP tối đa** (`eldenring_core_fp` dùng chung cho Ash of War, Cerulean Flask & Spirit Ashes).
  - **Endurance (Thể lực):** Tăng **Stamina tối đa & tốc độ hồi Stamina của Epic Fight** + tăng giới hạn **Equip Load** (`EquipLoadManager`).
  - **Strength (Sức mạnh):** Tăng sát thương cho Đại kiếm / Kích / Vũ khí nặng (*Starscourge, Golden Halberd, Maliketh's Black Blade, Blasphemous Blade*).
  - **Dexterity (Khéo léo):** Tăng sát thương cho Katana / Kiếm cong / Thương (*Rivers of Blood, Hand of Malenia, Bloodhound's Fang, Bolt of Gransax*).
  - **Intelligence (Trí tuệ):** Tăng sát thương phép thuật & Ash of War phép (*Moonveil, Dark Moon Greatsword*).
- Nghỉ tại Site of Grace đồng thời hồi đầy HP/FP/Flask và đặt lại điểm Respawn.

---

## 2. Hệ Thống Talisman — Bùa Hộ Mệnh *(Chuẩn 100% Elden Ring, gắn qua Curios API)*
- Trong Elden Ring gốc, **Talisman** thay cho hệ Nhẫn của Dark Souls — mỗi người chơi đeo tối đa **4 Talisman** (khởi đầu 1 ô, mở dần thêm ô qua *Talisman Pouch* khi hạ **Margit**, có **2 Great Runes**, và hạ **Morgott/Godfrey**!).
- Tận dụng luôn **Curios API** đã có sẵn trong Modpack để tạo slot `talisman` (1 → 4 ô):
  - **Crimson Amber Medallion:** +15% Máu tối đa.
  - **Cerulean Amber Medallion:** +20% FP tối đa.
  - **Green Turtle Talisman:** Tăng tốc hồi **Stamina Epic Fight**.
  - **Great-Jar's Arsenal:** +25% giới hạn **Equip Load** (mặc giáp nặng vẫn Medium Roll!).
  - **Shard of Alexander:** +20% sát thương kỹ năng **Ash of War**.
  - **Lord of Blood's Exultation:** Tăng +20% sát thương trong 15s khi kích hoạt **Bleed** gần đó.
  - **Dragoncrest Greatshield Talisman:** Giảm 20% sát thương vật lý nhận vào.

---

## 3. Co-op Boss Scaling + Poise / Riposte Nối Với Epic Fight
- **Co-op Boss Scaling *(bắt buộc cho chơi nhóm)*:**
  - Khi trận Boss bắt đầu (hoặc có người chơi bước qua **Fog Gate** vào đấu trường), tự đếm số người chơi trong phạm vi đấu trường:
    - 1 người: Máu `1.0x`, Poise `1.0x`
    - 2 người: Máu `1.5x`, Poise `1.25x`
    - 3 người: Máu `2.0x`, Poise `1.5x`
    - 4+ người: Máu `2.5x`, Poise `1.75x`
  - Chia thưởng **Runes + Remembrance** công bằng cho toàn bộ đồng đội tham gia hạ Boss (kết hợp **Lootr** cho rương riêng từng người!).
- **Tích hợp Epic Fight ↔ Boss (`part/combat-mechanics`):**
  - Đồng bộ i-frames lăn né của Epic Fight với hitbox arc/AoE của `EldenBossEntity`.
  - Đánh đòn nặng / Guard Counter / Ash of War tích **Poise Break** → Boss gục gối (`STAGGERED`) kèm âm thanh vỡ thế → người chơi lao vào đánh **Critical Riposte**!

---

## 4. Giao Diện UI & Âm Thanh Chuẩn Elden Ring (`part/audio-ui-qa`)
- **Màn hình thông báo toàn màn hình chuẩn game gốc:**
  - Chết: dải nền đen mờ + chữ đỏ **`YOU DIED`** kèm âm thanh trầm.
  - Hạ Boss: chữ vàng ánh sáng lan ngang màn hình + tiếng trống rền:
    - **`GREAT ENEMY FELLED`** *(Tree Sentinel, Margit, Fire Giant, Boss ngoài)*
    - **`DEMIGOD FELLED`** *(Godrick, Rennala, Radahn, Morgott, Rykard, Mohg, Malenia)*
    - **`LEGEND FELLED`** *(Maliketh, Godfrey)*
    - **`GOD SLAIN`** *(Radagon / Elden Beast)*
  - Vào vùng mới: chữ lớn giữa màn hình + tên vùng (đã có nền tảng trong `RegionDifficultyManager`).
- **HUD Góc Màn Hình Kiểu Elden Ring:**
  - Góc trên trái: 3 thanh mảnh **Đỏ (HP)** – **Xanh dương (FP)** – **Xanh lá (Stamina Epic Fight)**, độ dài thanh tự giãn ra khi nâng Vigor / Mind / Endurance!
  - Góc dưới trái: Cụm 4 ô nhanh kiểu chữ thập (Vũ khí tay phải, Vũ khí/Khiên tay trái, Bình Flask / Spirit Bell, Kỹ năng Ash of War đang cầm).
  - Góc dưới phải: Bộ đếm **Runes** đang giữ.
  - Thanh máu Boss nằm ngang dưới màn hình có tên Boss + tổng sát thương vừa gây ra.
- **Âm thanh:** Nhạc nền Boss riêng, tự chuyển giai điệu dồn dập khi Boss bước vào **Phase 2**.

---

## 5. Chuông Triệu Hồi Linh Hồn — Spirit Ashes (`Spirit Calling Bell`)
- Sử dụng thanh **FP** (`eldenring_core_fp`), chỉ kích hoạt được trong vùng đấu trường Boss / khu vực nguy hiểm (mỗi trận triệu hồi 1 lần, tự biến mất khi Boss chết hoặc người chơi rời đấu trường):
  - **Lone Wolf Ashes** *(nhận sớm ở Limgrave — tốn 55 FP):* Triệu hồi **3 Sói Linh Hồn** tốc độ cao giúp hút aggro & tích Poise nhẹ.
  - **Banished Knight Engvall** *(nhặt ở hầm ngục Limgrave/Liurnia — tốn 100 FP):* Hiệp sĩ giáp nặng trâu bò đỡ đòn thay.
  - **Mimic Tear Ashes** *(phần thưởng nhánh phụ Nokstella — tốn 40% HP thay vì FP, đúng 100% lore Elden Ring!):* Triệu hồi **bản sao của chính người chơi** cầm đúng vũ khí & giáp bạn đang mặc!

---

## Đề Xuất Mod QoL Miễn Phí Bổ Sung Cho Modpack (`modpack/mods.json`)
- **Shoulder Surfing Reloaded** — Camera góc nhìn thứ 3 qua vai chuẩn Elden Ring khi đánh Epic Fight.
- **JEI (Just Enough Items)** — Tra công thức chế tạo / nâng cấp.
- **Jade** — Xem tên, máu, giáp và hiệu ứng trên quái/boss.
- **FTB Quests** — Sổ nhiệm vụ 7 Chương + Quest phụ đồng bộ co-op.
- **Embeddium** *(+ tùy chọn Oculus)* — Tăng 2–3x FPS & hỗ trợ Shader.
