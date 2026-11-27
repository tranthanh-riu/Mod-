# Phần 2: Vũ khí, Ash of War & Hệ thống Combat (`combat/`)

![Weapons Concept](images/weapons_concept.jpg)

## Cơ chế chiến đấu
- Kết hợp **Epic Fight** (Roll i-frames, Stamina, Parry, Lock-on, Combo theo dòng vũ khí) với **`EldenWeaponItem`** & **`StatusBuildupManager`**.
- **Status Build-up**: Đòn đánh tích lũy thanh ẩn (mốc 100) trước khi kích nổ:
  - **Bleed**: Nổ 12% máu tối đa.
  - **Frostbite**: Nổ 9% máu tối đa + làm chậm & yếu giáp.
  - **Scarlet Rot**: Rút máu liên tục.

## Vũ khí đã khởi tạo trong khung Core
| Vũ khí | Ash of War (Chuột phải) | Hiệu ứng | Stance Damage |
| :--- | :--- | :--- | :--- |
| **Rivers of Blood** | `Corpse Piler` (chém vệt máu tầm trung) | Bleed | 18 |
| **Moonveil** | `Transient Moonlight` (phóng sóng ánh trăng) | Bleed | 20 |
| **Dark Moon Greatsword** | `Moonlight Wave` (phóng sóng băng) | Frostbite | 34 |
| **Hand of Malenia** | `Waterfowl Dance` (lướt chém lốc liên hoàn) | Bleed | 22 |

## Nhiệm vụ Port & Mở rộng
- Trích xuất model/texture từ *Elden Ring Weapons Pack* (1.19.2) trong `reference/` và đăng ký mới tại `ModItems.java`.
- Đồng bộ giáp từ *Elden Ring Nightreign Gear* (1.20.1) vào Loot Table của từng Đại Boss.
