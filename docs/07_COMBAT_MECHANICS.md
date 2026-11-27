# Kế Hoạch Chi Tiết — Phần 6: Cơ Chế Chiến Đấu Souls-like (`part/combat-mechanics`)

- **Nhánh Git**: `part/combat-mechanics`
- **Thư mục sở hữu (không đụng `part/weapons-combat`)**:
  - `src/main/java/com/eldenringcore/combat/mechanics/` *(Stamina, Dodge/Roll i-frames, Parry, Guard Counter, Lock-on, Critical/Riposte)*
  - `src/main/java/com/eldenringcore/client/combat/` *(HUD thanh Stamina, dấu khóa mục tiêu Lock-on, phím tắt Keybinding)*
  - `src/main/java/com/eldenringcore/mixin/` *(Mixin chặn sát thương trong i-frames, chỉnh tốc độ roll/camera)*

> **Quy tắc tách rạch ròi với `part/weapons-combat`:**
> - `part/weapons-combat` phụ trách **Vũ khí 3D, Ash of War, FP, Nâng cấp Smithing & Equip Load**.
> - `part/combat-mechanics` phụ trách **Thao tác chiến đấu cốt lõi của nhân vật** (Stamina, Né đòn Roll/i-frames, Đỡ đòn/Parry, Khóa mục tiêu Lock-on, Phá thế Riposte & HUD chiến đấu).

---

## Checklist Công Việc Theo Từng Giai Đoạn

### Giai đoạn 1 — Stamina & Né Đòn (Roll / Dodge i-frames) *(Ưu tiên số 1)*
- [ ] **Hệ thống Stamina (`StaminaManager.java`)**:
  - Lưu Stamina (mặc định `100.0f`), tiêu hao khi đánh, chạy nhanh, đỡ đòn và né đòn.
  - Tự hồi nhanh sau 0.8 giây không dùng (chậm lại khi đang giơ khiên đỡ).
  - Hiển thị thanh **Stamina màu xanh lá** trên HUD kiểu Elden Ring.
- [ ] **Dodge / Roll có i-frames (`DodgeRollManager.java` + Keybinding `Space` / `Left Alt`)**:
  - Nhấn phím né → lướt/lăn theo hướng di chuyển (WASD), tiêu tốn 20 Stamina.
  - Cấp **khung bất tử (i-frames)** trong ~8–12 tick (0.4–0.6 giây) giúp né xuyên đòn quét/AoE của Boss!
  - Đọc trọng lượng từ `EquipLoad` (nếu có): Light Roll lướt xa & nhiều i-frames, Medium Roll chuẩn, Heavy Roll (Fat Roll) chậm & ngắn.

### Giai đoạn 2 — Đỡ Đòn, Parry, Guard Counter & Critical (Riposte)
- [ ] **Hệ thống Đỡ đòn (Guard) & Guard Counter**:
  - Đỡ bằng khiên hoặc vũ khí 2 tay sẽ trừ **Stamina** thay vì trừ máu; hết Stamina bị **Guard Break** (choáng 1 giây).
  - Đánh ngay trong vòng 0.6 giây sau khi đỡ trúng đòn → kích hoạt **Guard Counter** (gây Stance/Poise Damage cực lớn lên Boss + tiếng *KENG!* đặc trưng).
- [ ] **Parry (Phản đòn chuẩn nhịp)**:
  - Nhấn nút Parry đúng cửa sổ 0.25 giây trước khi đòn đánh chạm tới → vô hiệu hóa sát thương, phá vỡ tư thế kẻ địch ngay lập tức.
- [ ] **Critical Hit / Riposte**:
  - Khi Boss đang ở trạng thái `BossState.STAGGERED` (quỳ gối vỡ Poise), nhấn đánh cận chiến ở khoảng cách < 2.5 block sẽ tung đòn **Riposte** (sát thương cực lớn + hiệu ứng nổ máu/ánh sáng + tiếng chí mạng nặng).

### Giai đoạn 3 — Khóa Mục Tiêu (Lock-on) & Tích Hợp Epic Fight / Camera
- [ ] **Lock-on Target (`LockOnHandler.java` — nhấn chuột giữa / phím `R`)**:
  - Khóa camera xoay mượt về phía Boss/quái gần nhất trong tầm 24 block, hiện chấm tròn trắng phát sáng giữa thân mục tiêu.
  - Tự động mở khóa nếu mục tiêu chết hoặc chạy quá xa.
- [ ] **Đồng bộ với *Epic Fight* & *Shoulder Surfing Reloaded***:
  - Đảm bảo không bị trùng phím tắt và hitbox i-frames ăn khớp 100% với đòn `performArcHitbox` / `performAoeHitbox` trong `EldenBossEntity.java`.

---

## Tiêu Chí Hoàn Thành (Definition of Done)
1. Chạy `.\gradlew.bat build --no-daemon` bằng **JDK 17** đạt **0 lỗi**.
2. Vào game Survival đánh thử với **Margit / Malenia**: lăn xuyên được đòn chém nhờ i-frames, đỡ đòn trừ Stamina chuẩn, và Lock-on xoay camera mượt không giật.
