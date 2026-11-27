# Phần 4: Hướng dẫn Chia nhóm & Quy trình Git

## 5 Vai trò làm việc song song (không đè file nhau)
1. **Boss & AI** → `src/main/java/com/eldenringcore/boss/`
2. **3D Model & Art (Blockbench)** → `src/main/resources/assets/eldenring_core/geo/`, `animations/`, `textures/`
3. **Vũ khí & Combat** → `src/main/java/com/eldenringcore/combat/`
4. **Map, Cơ chế & Modpack** → `src/main/java/com/eldenringcore/mechanic/`, `src/main/resources/data/eldenring_core/`
5. **Âm thanh, UI & Playtest** → `src/main/resources/assets/eldenring_core/sounds/`, `lang/`

## Quy trình Git hàng ngày
```bash
# 1. Lấy code mới nhất từ main
git checkout main
git pull origin main

# 2. Tạo nhánh riêng cho phần việc của bạn
git checkout -b boss/tree-sentinel

# 3. Sau khi làm xong, commit và đẩy nhánh lên GitHub
git add .
git commit -m "Add Tree Sentinel boss AI"
git push -u origin boss/tree-sentinel
```
Sau đó mở **Pull Request** trên GitHub để ghép vào `main`.
