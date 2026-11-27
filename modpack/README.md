# Modpack Elden Ring Core

Mod `eldenring_core` chạy được một mình (13 Boss, vũ khí, Rune, Grace, Fog Gate, map Wave 1–4).
Thế giới 7 vùng **Lands Between** cần thêm Terralith và các mod công trình/boss, nên nằm ở **modpack**, giống cách DawnCraft làm.

## Cấu trúc

| Đường dẫn | Nội dung |
|---|---|
| `mods.json` | Danh sách 24 mod, **khóa đúng phiên bản** + hash (Epic Fight, GeckoLib, Open Loader, Terralith, Cataclysm, Dungeons Arise, YUNG's...). |
| `overrides/config/openloader/data/eldenring_worldgen/` | Datapack dựng Lands Between: dimension `eldenring_core:lands_between` (biome Terralith) + biome tag / `structure_set` cho 11 mod công trình & boss. Open Loader tự bật cho mọi thế giới. |
| `build-mrpack.ps1` | Đóng gói ra `dist/EldenRingCore-<version>.mrpack`. |

**Quy tắc:** dữ liệu tham chiếu mod khác (`terralith:`, `cataclysm:`...) chỉ để trong `overrides/`, **không** để trong `src/main/resources` — nếu không, người chỉ cài mod sẽ không vào được thế giới.

## Chạy thử khi dev

```powershell
.\gradlew.bat runClient -Pmodpack
```

`-Pmodpack` làm Gradle tự tải đúng 24 mod trong `mods.json` và chép `overrides/` vào `run/`. Không có `-Pmodpack` thì chỉ chạy mod mình (build nhanh).

## Đóng gói & chia sẻ

```powershell
.\gradlew.bat build --no-daemon
powershell -ExecutionPolicy Bypass -File modpack\build-mrpack.ps1
```

Gửi file `.mrpack` cho cả nhóm → mở bằng **Modrinth App** hoặc **Prism Launcher** (Import) → launcher tự tải các mod từ trang gốc.
Chơi chung: cùng một bản `.mrpack`, một người mở LAN (qua Radmin VPN/ZeroTier) hoặc dựng server.

## Thêm / đổi mod

1. Thêm mục vào `mods.json` (lấy `project_id`, `version_id`, `url`, `sha1`, `sha512`, `size` từ trang Modrinth của bản Forge 1.20.1).
2. Nếu cần chỉnh worldgen cho mod đó, thêm file vào datapack trong `overrides/`.
3. Chạy `runServer -Pmodpack` kiểm tra, **và** chạy `runServer` không có cờ để chắc mod một mình vẫn vào được.
4. Tăng `version` trong `mods.json` trước khi phát hành bản mới.
