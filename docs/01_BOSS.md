# Kế Hoạch Chi Tiết — Phần 1: Boss & AI (`part/boss-ai`)

![Boss Concept](images/boss_concept.jpg)

- **Nhánh Git**: `part/boss-ai`
- **Thư mục code**: `src/main/java/com/eldenringcore/boss/`
- **File đăng ký chung**: `ModEntities.java`, `ModItems.java` (Spawn Egg), `EldenRingCore.java` (Attributes)

---

## Checklist Công Việc Theo Từng Wave

### Wave 1 — Limgrave & Stormveil *(Ưu tiên cao nhất)*
- [x] **Khung dùng chung `EldenBossEntity`**: State Machine, Delayed Attack, Phase 2, Poise Break, Arc/AoE Hitbox, Boss Bar & Banner.
- [x] **Margit, the Fell Omen (`MargitEntity`)**: Khung 4 chiêu cơ bản + Phase 2.
- [ ] **Hoàn thiện Margit (`MargitEntity`)**:
  - [ ] Thêm chiêu `tail_sweep` (quét đuôi khi người chơi đứng sau lưng).
  - [ ] Thêm chiêu `holy_sword_combo` ở Phase 2 (chém liên hoàn 3 nhịp kiếm ánh sáng).
  - [ ] Gắn `GeoEntityRenderer` (client) kết nối với model từ nhánh `part/art-models`.
  - [ ] Gắn Loot Table: rơi *Margit's Shackle* / *Somber Smithing Stone* + Runes.
- [ ] **Tree Sentinel (`TreeSentinelEntity`)**:
  - [ ] Tạo class kế thừa `EldenBossEntity` (máu ~600, giáp cao, Poise 160).
  - [ ] Moveset: `halberd_charge` (phi ngựa húc), `halberd_sweep` (quét rộng), `shield_bash` (đập khiên AoE Phase 2), `magic_reflect` (phản phép thuật).
- [ ] **Godrick the Grafted (`GodrickEntity`)**:
  - [ ] Moveset Phase 1: `axe_whirlwind` (lốc gió), `delayed_axe_slam` (đập rìu chậm), `earthquake_slam` (đập đất 2 nhịp AoE).
  - [ ] Moveset Phase 2 (< 50% HP): gắn đầu rồng phun lửa (`dragon_fire_breath` hình nón + `fire_tornado`).

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
