# Kế Hoạch Chi Tiết — Phần 3: Bản Đồ, Đấu Trường & Cơ Chế (`part/map-mechanics`)

![Lands Between Map](images/map_lands_between.svg)

- **Nhánh Git**: `part/map-mechanics`
- **Thư mục code**: `src/main/java/com/eldenringcore/mechanic/`
- **Thư mục data**: `src/main/resources/data/eldenring_core/` (Loot Table, Recipes, Tags)

---

## Checklist Công Việc Theo Từng Giai Đoạn

### Giai đoạn 1 — Công cụ Adventure Map & Cơ chế Sinh tồn *(Ưu tiên)*
- [x] **Site of Grace (`SiteOfGraceBlock`)**: Nghỉ ngơi hồi máu, hồi Flask, đặt respawn, hiện banner *LOST GRACE DISCOVERED*.
- [x] **Flask of Crimson Tears (`CrimsonFlaskItem`)**: 5 bình máu, hồi lại ở Site of Grace.
- [x] **Golden Fog Gate (`FogGateBlock`)**: Cổng sương vàng cho đấu trường.
- [x] **Boss Arena Controller (`BossArenaBlock` + `BossArenaBlockEntity`)**: Tự triệu hồi Boss, tự reset nếu chết, tự mọc Site of Grace khi thắng.
- [x] **Nâng cấp Boss Arena Controller**:
  - [x] Tự động đóng/mở **Fog Gate** xung quanh đấu trường khi trận đấu bắt đầu/kết thúc.
  - [x] Cho phép nhóm Map đổi `BossId` dễ dàng bằng lệnh `/data merge block` hoặc item cấu hình trong Creative.
- [x] **Hệ thống Runes rơi khi chết**:
  - [x] Lưu lượng Runes của người chơi; khi chết tạo 1 thực thể cột sáng vàng tại chỗ chết.
  - [x] Chạm vào để nhặt lại; nếu chết lần 2 trước khi nhặt thì mất sạch.
- [x] **Bình Mana (`Flask of Cerulean Tears`)**: Hồi FP/Mana dùng cho Ash of War & Phép thuật.
- [x] **Site of Grace Fast Travel**: Đặt tên từng điểm Grace trên map và mở GUI dịch chuyển qua lại giữa các Grace đã khám phá.

### Giai đoạn 2 — Xây dựng Adventure Map "The Lands Between" (Wave 1 → 4)
*(Nhóm Builder dùng **WorldEdit / Axiom** trên Minecraft 1.20.1 Forge)*
- [x] **Khu vực Wave 1 — Limgrave & Stormveil**:
  - [x] Điểm xuất phát **The First Step** + Site of Grace đầu tiên.
  - [x] Khu vực mở đặt **Boss Arena Controller (`tree_sentinel`)**.
  - [x] **Church of Elleh** (khu rèn vũ khí bằng Smithing Stones).
  - [x] **Cầu Stormveil** đặt Fog Gate + **Boss Arena Controller (`margit`)**.
  - [x] **Lâu đài Stormveil** dẫn tới sân đấu **Godrick**.
  - Cài đặt: `/function eldenring_core:wave1/install` (một lần), vào map: `/function eldenring_core:wave1/enter`. Boss `tree_sentinel` và `godrick` chờ nhóm Boss đăng ký entity.
- [x] **Khu vực Wave 2 — Liurnia & Caelid**: Học viện Raya Lucaria (Rennala) & Đấu trường cát đỏ (Radahn).
  - Cài đặt: `/function eldenring_core:wave2/install`, vào map: `/function eldenring_core:wave2/enter`. Arena: `rennala` (Grand Library, 2 Fog Gate), `radahn` (Wailing Dunes).
- [ ] **Khu vực Wave 3–4 — Leyndell, Cây Erdtree khổng lồ & Haligtree**: Dựng cây Erdtree phát sáng nhìn thấy từ xa bằng *Distant Horizons*.

### Giai đoạn 3 — Chắt lọc Modpack *EldenCraft* & Vá Xung Đột
- [ ] Lọc danh sách mod cần giữ từ *EldenCraft* (ưu tiên: *Epic Fight*, *Iron's Spells 'n Spellbooks*, *Shoulder Surfing*, *AmbientSounds 6*, *Distant Horizons*, *Nightreign Gear*).
- [ ] Loại bỏ các mod sinh địa hình ngẫu nhiên/mod thừa không dùng cho Adventure Map.
- [ ] Chạy thử chung toàn bộ mod + `eldenring_core`, kiểm tra `latest.log` và vá xung đột (keybind, Mixin, cân bằng sát thương).
