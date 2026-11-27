# Kế Hoạch Chi Tiết — Phần 1: Boss & AI (`part/boss-ai`)

![Boss Concept](images/boss_concept.jpg)

- **Nhánh Git**: `part/boss-ai`
- **Thư mục code**: `src/main/java/com/eldenringcore/boss/`
- **File đăng ký chung**: `ModEntities.java`, `ModItems.java` (Spawn Egg), `EldenRingCore.java` (Attributes)

---

## Checklist Công Việc Theo Từng Wave

### Wave 1 — Limgrave & Stormveil *(Ưu tiên cao nhất)*
- [x] **Khung dùng chung `EldenBossEntity`**: State Machine, Delayed Attack, Phase 2, Poise Break, Arc/AoE Hitbox, Boss Bar & Banner.
- [x] **Margit, the Fell Omen (`MargitEntity`)**: Đủ 6 chiêu (*Delayed Cane Slam, Golden Dagger Combo, Tail Sweep, Glintblade Throw, Phase 2 Hammer Leap, Holy Sword 3-Hit Combo*) + Loot rơi.
- [x] **Tree Sentinel (`TreeSentinelEntity`)**: Đủ 5 chiêu (*Halberd Charge, Wide Sweep, Delayed Smash, Phase 2 Golden Shield Slam, Erdtree Retaliation*) + Loot rơi.
- [x] **Godrick the Grafted (`GodrickEntity`)**: Đủ 4 chiêu (*Delayed Axe Cleave, Storm Whirlwind, 2-Hit Earthquake Slam, Phase 2 Dragon Fire Breath* + câu thoại *"BEAR WITNESS!"*) + Loot rơi.
- [x] **Gắn GeckoLib Client Renderer (`EldenBossRenderer` + `ClientModEvents`)**: Sẵn sàng nạp model từ nhánh `part/art-models` cho cả 3 Boss Wave 1.

### Wave 2 — Liurnia & Caelid
- [ ] **Rennala, Queen of the Full Moon (`RennalaEntity`)**: Phase 1 cơ chế khiên bảo vệ + Phase 2 bắn phép *Comet Azur*, triệu hồi tinh linh (*Spirit Summons*).
- [ ] **Starscourge Radahn (`RadahnEntity`)**: Bắn cung trọng lực tầm xa, kéo hút trọng lực (*Starcaller Cry*), Phase 2 bay lên trời rồi lao thiên thạch nổ cực lớn.

### Wave 3 — Leyndell, Volcano & Mohgwyn
- [ ] **Morgott, the Omen King**: Nâng cấp từ khung Margit + mưa kiếm ánh sáng (*Cursed-Blood Slice*).
- [ ] **Rykard, Lord of Blasphemy**: Đòn quét lửa diện rộng + hộp sọ nổ đuổi theo người chơi.
- [ ] **Mohg, Lord of Blood**: Cơ chế đếm 3 vòng máu (*NIHIL!*) gây Bleed toàn đấu trường ở Phase 2.
- [ ] **Fire Giant**: Kích thước khổng lồ, điểm yếu ở mắt cá chân, Phase 2 cầu lửa lăn.

### Wave 4 — Endgame (Đỉnh cao)
- [ ] **Malenia, Blade of Miquella**: Hồi máu mỗi khi đánh trúng người chơi (kể cả khi đỡ khiên), chiêu *Waterfowl Dance* 3 đợt chém lốc, Phase 2 *Scarlet Aeonia* (nổ hoa Scarlet Rot).
- [ ] **Maliketh, the Black Blade**: Tốc độ cao, nhảy bám trụ, hiệu ứng *Destined Death* rút máu tối đa.
- [ ] **Godfrey / Hoarah Loux**: Phase 1 rìu & dậm đất toàn sân, Phase 2 vật cận chiến.
- [ ] **Radagon & Elden Beast**: Chuyển tiếp 2 Boss liên tiếp.

---

## Tiêu Chí Hoàn Thành Mỗi Con Boss (Definition of Done)
1. Có ít nhất **4 chiêu thức** (có chiêu cận chiến, chiêu tầm xa/áp sát, chiêu *delayed attack*).
2. Có **Phase 2** rõ ràng (đổi nhạc, hiệu ứng chuyển phase, mở khóa chiêu mới).
3. Test trong game bằng `.\gradlew.bat runClient`: né được bằng Roll, phá được Poise, hạ xong hiện banner vàng và rơi đúng đồ.
