# Kế Hoạch Chi Tiết — Phần 5: Âm Thanh, UI/HUD & Playtest (`part/audio-ui-qa`)

- **Nhánh Git**: `part/audio-ui-qa`
- **Không cần biết code Java!** (Chỉ cần chỉnh file `.ogg`, file JSON văn bản và chơi test game).
- **Thư mục sở hữu**:
  - Âm thanh: `src/main/resources/assets/eldenring_core/sounds/` và `sounds.json`
  - Ngôn ngữ: `src/main/resources/assets/eldenring_core/lang/vi_vn.json` & `en_us.json`

---

## Checklist Công Việc

### 1. Chuẩn bị Âm thanh (Định dạng `.ogg` — dùng Audacity xuất)
> **Lưu ý kỹ thuật Minecraft:** Âm thanh hiệu ứng 3D (SFX quái/kiếm) nên xuất **Mono**, nhạc nền Boss/UI xuất **Stereo**, định dạng **Ogg Vorbis (`.ogg`)**.

- [ ] **SFX Hệ thống Elden Ring**:
  - [ ] `sounds/ui/you_died.ogg` *(tiếng trầm đặc trưng khi chết)*
  - [ ] `sounds/ui/demigod_felled.ogg` *(tiếng vang khi hạ Boss)*
  - [ ] `sounds/ui/grace_discovered.ogg` *(tiếng chuông sáng khi chạm Site of Grace)*
  - [ ] `sounds/combat/stance_break.ogg` *(tiếng "KENG!" nặng khi phá Poise Boss)*
  - [ ] `sounds/combat/parry.ogg` & `critical_hit.ogg`
  - [ ] `sounds/combat/bleed_proc.ogg` & `frostbite_proc.ogg`
- [ ] **Nhạc nền Boss (Wave 1 trước)**:
  - [ ] `sounds/music/boss_margit.ogg`
  - [ ] `sounds/music/boss_tree_sentinel.ogg`
  - [ ] `sounds/music/boss_godrick.ogg`
- [ ] Tạo file `assets/eldenring_core/sounds.json` đăng ký toàn bộ âm thanh trên.

### 2. Việt hóa & UI/HUD
- [ ] Kiểm tra & dịch mượt toàn bộ tên Boss, Vũ khí, Chiêu thức, Câu thoại Boss trong `vi_vn.json` (và giữ chuẩn tên tiếng Anh trong `en_us.json`).
- [ ] Đề xuất font chữ / màu sắc thanh máu Boss và màn hình **YOU DIED** sao cho giống game gốc nhất.

### 3. Quy trình Playtest & Cân bằng (QA)
Mỗi khi có bản build mới (hoặc sau khi merge PR vào `main`), test theo 5 mục sau và ghi lại phản hồi cho nhóm Code:
- [ ] **Độ mượt Combat**: Roll có né được đòn quét của Margit không? Hitbox có bị "đánh hụt vẫn dính" không?
- [ ] **Cân bằng độ khó**: Máu Boss (hiện Margit = 450 HP) và sát thương (14–22) có quá dễ hay quá khó khi mặc giáp *Nightreign Gear*?
- [ ] **Hiệu ứng Vũ khí**: *Bleed / Frostbite* có nổ quá nhanh làm Boss chết trong 5 giây không?
- [ ] **Boss Arena**: Chết có tự reset Boss không? Thắng có tự mọc Site of Grace không?
- [ ] **Hiệu năng & Crash**: Ghi lại file `run/logs/latest.log` nếu có bất kỳ lỗi đỏ hoặc crash nào.
