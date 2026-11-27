# Phần 1: Hệ thống Đại Boss Elden Ring (`boss/`)

![Boss Concept](images/boss_concept.jpg)

## Kiến trúc Boss Framework
Tất cả Đại Boss kế thừa từ `EldenBossEntity` (`src/main/java/com/eldenringcore/boss/EldenBossEntity.java`):
- **State Machine AI**: `IDLE` → `APPROACH` → `TELEGRAPH` (hỗ trợ **Delayed Attack**) → `ATTACKING` → `RECOVERY`, kèm `PHASE_TRANSITION` & `STAGGERED`.
- **Hitbox theo frame**: `performArcHitbox(...)` (quét hình nón) & `performAoeHitbox(...)` (đập diện rộng).
- **Poise / Stance Break**: Đánh đủ lực phá thế giúp người chơi tung đòn chí mạng (x1.75 sát thương).
- **Boss Bar & Victory Banner**: Hiện thanh máu Boss và dòng chữ vàng **GREAT ENEMY FELLED / DEMIGOD FELLED**.

## Danh sách Boss theo từng Wave
- **Wave 1**: `MargitEntity` *(đã có khung code)*, `TreeSentinelEntity`, `GodrickEntity`
- **Wave 2**: `RennalaEntity`, `RadahnEntity`
- **Wave 3**: `MorgottEntity`, `RykardEntity`, `MohgEntity`, `FireGiantEntity`
- **Wave 4**: `MaleniaEntity`, `MalikethEntity`, `GodfreyEntity`, `RadagonEntity`

## Quy ước cho nhóm Art (Blockbench / GeckoLib)
- Đặt file model tại: `src/main/resources/assets/eldenring_core/geo/<boss_id>.geo.json`
- Đặt animation tại: `src/main/resources/assets/eldenring_core/animations/<boss_id>.animation.json`
- Đặt texture tại: `src/main/resources/assets/eldenring_core/textures/entity/<boss_id>.png`
