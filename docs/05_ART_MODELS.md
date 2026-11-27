# Kế Hoạch Chi Tiết — Phần 4: 3D Model, Animation & Texture (`part/art-models`)

- **Nhánh Git**: `part/art-models`
- **Không cần biết code Java!** Chỉ cần dùng **[Blockbench](https://www.blockbench.net/)** + plugin **GeckoLib Animation Utils**.
- **Thư mục sở hữu**:
  - Model 3D Boss: `src/main/resources/assets/eldenring_core/geo/`
  - Animation Boss: `src/main/resources/assets/eldenring_core/animations/`
  - Texture (Boss, Item, Block): `src/main/resources/assets/eldenring_core/textures/`

---

## Quy Ước Đặt Tên File & Animation (Bắt buộc khớp với Code)

### 1. Model & Animation cho **13 Đại Boss (Waves 1–4)**
- [x] Đã tạo sẵn trọn bộ 13 file `.geo.json`, 13 file `.animation.json` và 13 file `.png` (64×64) bám sát ngoại hình gốc trong `assets/eldenring_core/`:
  - **Wave 1**: `margit` (sừng Omen, đuôi, áo choàng, gậy gỗ + dao vàng), `tree_sentinel` (cưỡi chiến mã giáp vàng + kích + khiên), `godrick` (vương miện, cánh tay ghép, rìu vàng + đầu rồng).
  - **Wave 2**: `rennala` (vương miện trăng cao + quyền trượng Glintstone), `radahn` (mũ sừng bờm đỏ + song kiếm + ngựa nhỏ Leonard).
  - **Wave 3**: `morgott`, `rykard` (mãng xà cuộn + Blasphemous Blade), `mohg` (sừng cong + cánh dơi + đinh ba), `fire_giant` (khổng lồ + mắt Fell God trước ngực + khiên chảo).
  - **Wave 4**: `malenia` (mũ cánh vàng + cánh bướm đỏ + katana dài), `maliketh` (giáp sói đen-vàng + Hắc Kiếm), `godfrey` (vương miện + rìu + sư tử linh hồn Serosh), `radagon` (tóc đỏ + vết nứt vàng + búa Marika).
- *(Nhóm Art có thể mở từng file `.geo.json` này trực tiếp trong **Blockbench** để tinh chỉnh/vẽ thêm chi tiết siêu nét mà không cần dựng xương lại từ đầu!)*

### 2. Texture & Model cho Vũ khí, Block & 13 Trứng Boss
- [x] Đã hoàn thiện toàn bộ pixel-art `.png` + model JSON cho: `rivers_of_blood`, `moonveil`, `dark_moon_greatsword`, `hand_of_malenia`, `flask_of_crimson_tears`, `smithing_stone`, `somber_smithing_stone`, `site_of_grace`, `fog_gate` và toàn bộ 13 `*_spawn_egg`.
