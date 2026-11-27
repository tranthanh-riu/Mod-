# Kế Hoạch Chi Tiết — Phần 4: 3D Model, Animation & Texture (`part/art-models`)

- **Nhánh Git**: `part/art-models`
- **Không cần biết code Java!** Chỉ cần dùng **[Blockbench](https://www.blockbench.net/)** + plugin **GeckoLib Animation Utils**.
- **Thư mục sở hữu**:
  - Model 3D Boss: `src/main/resources/assets/eldenring_core/geo/`
  - Animation Boss: `src/main/resources/assets/eldenring_core/animations/`
  - Texture (Boss, Item, Block): `src/main/resources/assets/eldenring_core/textures/`

---

## Quy Ước Đặt Tên File & Animation (Bắt buộc khớp với Code)

### 1. Model & Animation cho **Margit, the Fell Omen** *(Ưu tiên số 1)*
- File model: `assets/eldenring_core/geo/margit.geo.json`
- File animation: `assets/eldenring_core/animations/margit.animation.json`
- File texture: `assets/eldenring_core/textures/entity/margit.png`
- **Tên các clip animation bắt buộc phải đặt đúng trong Blockbench**:
  - [ ] `animation.boss.idle` *(đứng thở)*
  - [ ] `animation.boss.walk` *(đi bộ/tiến lại gần)*
  - [ ] `animation.boss.staggered` *(quỳ gối choáng khi bị phá Poise)*
  - [ ] `animation.boss.phase2` *(gồng năng lượng chuyển Phase 2)*
  - [ ] `animation.margit.delayed_slam` *(giơ gậy khựng lại rồi đập xuống)*
  - [ ] `animation.margit.dagger_combo` *(chém nhanh 2 nhịp dao vàng)*
  - [ ] `animation.margit.throw_dagger` *(vung tay ném dao ánh sáng)*
  - [ ] `animation.margit.hammer_leap` *(nhảy cao cầm búa vàng đập xuống đất)*

### 2. Texture & Model cho Vũ khí & Vật phẩm Wave 1
- [ ] `textures/item/rivers_of_blood.png` *(Katana đỏ thấm máu)*
- [ ] `textures/item/moonveil.png` *(Katana ánh trăng xanh)*
- [ ] `textures/item/dark_moon_greatsword.png` *(Đại kiếm băng phát sáng)*
- [ ] `textures/item/hand_of_malenia.png` *(Katana vàng dài)*
- [ ] `textures/item/flask_of_crimson_tears.png` *(Bình máu vàng-đỏ)*
- [ ] `textures/item/smithing_stone.png` & `somber_smithing_stone.png`
- *(Tùy chọn đẹp hơn)*: Dựng model 3D `.json` trong Blockbench cho các cây kiếm để cầm trên tay dài và ngầu như game gốc.

### 3. Texture & Model cho Block
- [ ] **Site of Grace** (`site_of_grace`): Dựng model 3D rễ cây xoắn + tia sáng vàng (Blockbench) để đặt trên map cực đẹp.
- [ ] **Golden Fog Gate** (`fog_gate`): Texture sương vàng bán trong suốt có hiệu ứng chảy (`.png` + `.png.mcmeta`).
- [ ] **Boss Arena Controller** (`boss_arena_controller`): Texture khối đá khắc hoa văn Elden Ring.

### 4. Boss tiếp theo (Wave 1 → 4)
- [ ] Wave 1: `tree_sentinel` (Hiệp sĩ cưỡi ngựa giáp vàng), `godrick` (Nhiều cánh tay + đầu rồng Phase 2).
- [ ] Wave 2: `rennala`, `radahn`.
- [ ] Wave 3–4: `morgott`, `rykard`, `mohg`, `malenia`, `maliketh`, `godfrey`, `radagon`.
