#!/usr/bin/env python3
"""
Downloads all 22 free 1.20.1 Forge runtime mods (Terralith, Structures, DawnCraft Boss Mods, Map & APIs)
from Modrinth into libs_dev/ so any teammate can run play_test.bat or test_all.bat immediately.
"""
import json
import os
import urllib.request

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
LIBS_DEV = os.path.join(ROOT, "libs_dev")
UA = {"User-Agent": "EldenRingCoreModDev/1.0"}

MODS = [
    # Biome & Terrain
    ("terralith", "Terralith_1.20.x_v2.5.4.jar"),
    # Structure & Dungeon Mods
    ("when-dungeons-arise", "DungeonsArise-1.20.x.jar"),
    ("structory", "Structory_1.20.x.jar"),
    ("structory-towers", "Structory_Towers_1.20.x.jar"),
    ("explorify", "explorify-1.20.1.jar"),
    ("yungs-api", "YungsApi-1.20-Forge.jar"),
    ("yungs-better-dungeons", "YungsBetterDungeons-1.20-Forge.jar"),
    ("yungs-better-strongholds", "YungsBetterStrongholds-1.20-Forge.jar"),
    ("yungs-better-mineshafts", "YungsBetterMineshafts-1.20-Forge.jar"),
    # DawnCraft-style Boss & Field Boss Mods + APIs
    ("curios", "curios-forge-1.20.1.jar"),
    ("lionfish-api", "lionfishapi-1.20.1.jar"),
    ("l_enders-cataclysm", "L_Enders_Cataclysm-1.20.1.jar"),
    ("mowzies-mobs", "mowziesmobs-1.20.1.jar"),
    ("cerbons-api", "CerbonsAPI-Forge-1.20.1.jar"),
    ("cloth-config", "cloth-config-forge-1.20.1.jar"),
    ("bosses-of-mass-destruction-forge", "BOMD-Forge-1.20.1.jar"),
    ("villagerstradingplus", "villagerstradingplus-1.20.1.jar"),
    ("the-graveyard-forge", "graveyard-forge-1.20.1.jar"),
    ("borninchaos", "born_in_chaos-1.20.1.jar"),
    # Map & Builder Tools
    ("xaeros-minimap", "xaerominimap-forge-1.20.1.jar"),
    ("xaeros-world-map", "xaeroworldmap-forge-1.20.1.jar"),
    ("worldedit", "worldedit-forge-1.20.1.jar"),
]


def main():
    os.makedirs(LIBS_DEV, exist_ok=True)
    for slug, outname in MODS:
        dest = os.path.join(LIBS_DEV, outname)
        if os.path.exists(dest) and os.path.getsize(dest) > 10000:
            print(f"[SKIP] {outname} already exists.")
            continue
        url = f"https://api.modrinth.com/v2/project/{slug}/version?loaders=%5B%22forge%22%5D&game_versions=%5B%221.20.1%22%5D"
        req = urllib.request.Request(url, headers=UA)
        with urllib.request.urlopen(req, timeout=30) as r:
            vs = json.loads(r.read().decode())
        if not vs:
            print(f"[WARN] No 1.20.1 Forge version found for {slug}")
            continue
        v = vs[0]
        f = next((x for x in v["files"] if x["primary"]), v["files"][0])
        print(f"[GET ] {slug} ({v['version_number']}) -> {outname}")
        req2 = urllib.request.Request(f["url"], headers=UA)
        with urllib.request.urlopen(req2, timeout=180) as r2, open(dest, "wb") as out:
            out.write(r2.read())
        print(f"       OK ({os.path.getsize(dest):,} bytes)")
    print("\nDone! Run gradlew.bat prepareRunClient or play_test.bat to start.")


if __name__ == "__main__":
    main()
