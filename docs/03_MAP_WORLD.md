# Phần 3: Bản đồ, Thế giới & Cơ chế Sinh tồn (`mechanic/` & `data/`)

![Lands Between Map](images/map_lands_between.svg)

## Cơ chế Thế giới & Sinh tồn cốt lõi
- **Site of Grace (`SiteOfGraceBlock`)**: Điểm nghỉ ngơi — hồi đầy máu, hồi lại toàn bộ bình **Flask of Crimson Tears**, đặt điểm hồi sinh và hiện banner **LOST GRACE DISCOVERED**.
- **Golden Fog Gate (`FogGateBlock`)**: Cổng sương vàng ngăn cách đấu trường Boss.
- **Boss Arena Controller (`BossArenaBlock` + `BossArenaBlockEntity`)**: Công cụ dành cho nhóm dựng **Adventure Map cố định**:
  - Đặt trong đấu trường Boss (ví dụ cầu Stormveil của Margit).
  - Người chơi Survival/Adventure bước vào phạm vi → tự động triệu hồi Boss.
  - Nếu người chơi chết hoặc rời đấu trường → tự động reset Boss về trạng thái ban đầu.
  - Khi hạ gục Boss → tự động mọc ra 1 **Site of Grace** ngay trên đấu trường!
  - Chuột phải ở chế độ Creative để reset đấu trường test lại ngay lập tức.
- **Flask of Crimson Tears (`CrimsonFlaskItem`)**: Bình máu giới hạn lượt dùng (mặc định 5 bình), chỉ hồi lại khi nghỉ ở Site of Grace.
- **Runes rơi khi chết** *(đang lên kế hoạch Wave 1)*: Rơi toàn bộ Runes tại chỗ chết để quay lại nhặt; chết lần 2 trước khi nhặt thì mất.

## Thiết kế Map / Vùng đất
- **Limgrave & Stormveil** (Wave 1): Đồng cỏ u ám, cầu đá Margit, lâu đài Stormveil.
- **Liurnia & Caelid** (Wave 2): Học viện Raya Lucaria (sương xanh) & vùng đất mục rữa đỏ Caelid.
- **Leyndell & Endgame** (Wave 3–4): Kinh thành hoàng kim dưới gốc Erdtree & Haligtree.
- Kết hợp tinh chỉnh các mod địa hình/dungeon/âm thanh chọn lọc từ *EldenCraft*.
