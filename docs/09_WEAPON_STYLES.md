# Style Chiến Đấu Từng Vũ Khí (Epic Fight)

Mỗi vũ khí có **loại vũ khí Epic Fight riêng** trong `data/eldenring_core/capabilities/weapons/types/<vũ khí>.json` (combo, tư thế cầm, âm thanh, hiệu ứng trúng đòn), và file item `capabilities/weapons/<vũ khí>.json` trỏ tới nó (`"type": "eldenring_core:<vũ khí>"`). Bộ chiêu dựng từ animation có sẵn của Epic Fight, bám theo cách vũ khí đó đánh trong Elden Ring.

## Ash of War

- **Chế độ chiến đấu Epic Fight** (phím `R`): Ash of War dùng **phím "Weapon Innate Skill" của Epic Fight**; **chuột phải = Đỡ đòn** của Epic Fight.
- **Chế độ thường (đào khối):** chuột phải vẫn dùng Ash of War như cũ.
- Mỗi Ash of War phát một động tác Epic Fight (bảng dưới) rồi mới ra hiệu ứng của mình; vẫn tốn FP và có thời gian hồi như trước.
- Code: `combat/EpicFightAshAnimations.java` (bảng động tác), `client/combat/AshOfWarKeyHandler.java` (phím), `network/ModNetwork.java` (gói tin), `EldenWeaponItem.activateAshOfWar`.

## Bảng style

| Vũ khí | Loại | Tư thế cầm | Combo đánh thường | Ash of War → động tác | Trúng đòn |
|---|---|---|---|---|---|
| Rivers of Blood | katana | katana | chém nhanh 4 nhịp (uchigatana 1-2, tachi 2, uchigatana 3) | Corpse Piler → 3 nhát xoay `blade_rush_combo1/2/3` theo từng lần bấm | máu (eviscerate) |
| Moonveil | katana | **tra vỏ** | rút kiếm chém → 2 nhát katana | Transient Moonlight → rút kiếm `battojutsu` (ngồi: `battojutsu_dash` lao tới) | lưỡi kiếm |
| Hand of Malenia | katana dài | tachi | 6 nhát liên hoàn nhanh (tachi + kiếm) | Waterfowl Dance → `dancing_edge` | máu |
| Bloodhound's Fang | kiếm cong lớn | trường kiếm | vung cong nặng xen tachi/đại kiếm | Bloodhound's Finesse → lộn ngược `phantom_ascent_backward` | máu |
| Dark Moon Greatsword | đại kiếm | trường kiếm | 2 nhát trường kiếm thanh thoát + 1 nhát đại kiếm | Moonlight Wave → bổ 2 tay `greatsword_slam` | lưỡi kiếm |
| Blasphemous Blade | đại kiếm | đại kiếm | đại kiếm 1 → trường kiếm 3 → đại kiếm 2 | Taker's Flames → quét ngang `sweeping_edge` | lưỡi kiếm |
| Starscourge Greatsword | đại kiếm khổng lồ | đại kiếm | chậm, nặng: đại kiếm 2 → bổ rìu → đại kiếm 1 | Starcaller Cry → giơ kiếm `revelation_twohand` | va đập mạnh |
| Maliketh's Black Blade | đại kiếm | **thế thủ Liechtenauer** | 3 nhát thế thủ liên hoàn | Destined Death → xoay chém `steel_whirlwind` | lưỡi kiếm |
| Golden Halberd | kích | giáo | đâm → quét chém → đâm | Golden Vow → giơ vũ khí thề `everlasting_allegiance_call` | va đập |
| Bolt of Gransax | giáo | giáo | 3 cú đâm liên tiếp (trident) | Ancient Lightning Spear → ném lao `javelin_throw_mid` | lưỡi kiếm |

## Cần cân chỉnh khi test trong game

- Một số động tác kỹ năng của Epic Fight tự gây sát thương; cộng với sát thương Ash of War có thể hơi mạnh → giảm hệ số trong `EldenWeaponItem` nếu cần.
- Vệt chém (trail) màu riêng cho từng vũ khí chưa làm (file `item_skins` phía client của Epic Fight).
- Animation riêng hoàn toàn (không dùng của Epic Fight) là bước sau, cần làm bằng Blender + Epic Fight animation tool.

## Xuất Huyết (Hemorrhage)

- Khi thanh chảy máu đầy (Rivers of Blood, Moonveil, Hand of Malenia, Bloodhound's Fang): máu phun ra, rồi hiệu ứng **Xuất Huyết** trừ máu **từ từ**: 6 nhịp trong 3 giây, tổng **15% máu tối đa** (Boss: 8%), xuyên giáp, không đẩy lùi, có máu nhỏ giọt mỗi nhịp.
- Có biểu tượng hiệu ứng riêng, chết vì nó hiện "… đã chảy máu đến chết" (tên người đánh được ghi công).
- Code: `combat/HemorrhageEffect.java`, `registry/ModEffects.java`, damage type `data/eldenring_core/damage_type/hemorrhage.json`.

## Hiệu ứng phát sáng (bloom) cho skill

`combat/AshVfx.java` vẽ 3 lớp cho mỗi Ash of War, canh đúng lúc vung kiếm của animation: lõi tự phát sáng (end rod / tia lửa / lửa), quầng màu to mềm (dust cỡ lớn), và chớp sáng (flash) tại điểm ra đòn. Chạy với Minecraft gốc, không cần shader; bật shader (vd. Complementary) thì bloom thật còn rõ hơn.

| Ash of War | Màu / dạng |
|---|---|
| Transient Moonlight | trăng xanh: vầng cung 150°, ngồi = sóng thẳng |
| Moonlight Wave | sóng trăng trắng-xanh dài 10 block |
| Corpse Piler | lửa máu đỏ, mỗi nhịp nghiêng một góc khác |
| Waterfowl Dance | 3 vòng đỏ thắm + cánh hoa |
| Taker's Flames | vầng lửa cam vàng 170° |
| Starcaller Cry | vòng tím trọng lực + chớp lớn |
| Destined Death | vòng lửa đen-đỏ 360° |
| Bloodhound's Finesse | vệt lửa máu khi lộn |
| Golden Vow | vòng vàng + cột sáng |
| Ancient Lightning Spear | tia sét đỏ dài 16 block |
