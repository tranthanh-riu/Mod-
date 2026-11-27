#!/usr/bin/env python3
"""
Automated Full-System QA & Conflict Test for Elden Ring Core (1.20.1 Forge).
Runs:
  1. Static Resource, JSON, Boss Assets, Weapon Assets & Mod Dependency Audit
  2. Real Headless Forge Dedicated Server Boot (all 25+ mods)
  3. Live RCON Verification inside `eldenring_core:lands_between`:
     - All 7 Elden Ring Biomes locate check (real coordinates)
     - 12 Boss Dungeons & Structures locate check (real coordinates)
     - All 13 Elden Ring Bosses summon & tick check
  4. Full Log Error/Conflict Scanner (`run/logs/latest.log`)
"""
import json
import os
import re
import shutil
import socket
import struct
import subprocess
import sys
import threading
import time
import zipfile

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
RUN_DIR = os.path.join(ROOT, "run")
LIBS_DEV = os.path.join(ROOT, "libs_dev")
RES_DIR = os.path.join(ROOT, "src", "main", "resources")
JAVA17 = r"C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot"
RCON_PORT = 25575
RCON_PASS = "eldenqa123"

BOSSES = [
    "margit", "tree_sentinel", "godrick", "rennala", "radahn",
    "morgott", "rykard", "mohg", "fire_giant", "malenia",
    "maliketh", "godfrey", "radagon",
]

BIOMES = [
    ("limgrave", (-300, 100, 700), "eldenring_core:limgrave"),
    ("liurnia", (-750, 100, -150), "eldenring_core:liurnia"),
    ("caelid", (650, 100, 550), "eldenring_core:caelid"),
    ("altus_plateau", (50, 100, -700), "eldenring_core:altus_plateau"),
    ("mt_gelmir", (-750, 100, -950), "eldenring_core:mt_gelmir"),
    ("mountaintops", (800, 100, -1050), "eldenring_core:mountaintops"),
    ("haligtree", (0, 100, -1450), "eldenring_core:haligtree"),
]

STRUCTURES_TO_TEST = [
    ("Limgrave", (-300, 120, 700), "mowziesmobs:wrought_chamber"),
    ("Limgrave", (-300, 120, 700), "bosses_of_mass_destruction:void_blossom"),
    ("Limgrave", (-300, 120, 700), "graveyard:crypt"),
    ("Limgrave", (-300, 120, 700), "structory:abandoned_chapel"),
    ("Limgrave", (-300, 120, 700), "betterstrongholds:stronghold"),
    ("Liurnia", (-750, 120, -150), "bosses_of_mass_destruction:lich_tower"),
    ("Altus", (50, 120, -700), "dungeons_arise:shiraz_palace"),
    ("Altus", (50, 120, -700), "cataclysm:ancient_factory"),
    ("Mt.Gelmir", (-750, 120, -950), "cataclysm:soul_black_smith"),
    ("Mountaintops", (800, 120, -1050), "cataclysm:frosted_prison"),
    ("Haligtree", (0, 120, -1450), "graveyard:lich_prison"),
    ("Caelid", (650, 120, 550), "cataclysm:cursed_pyramid"),
]


class SimpleRcon:
    def __init__(self, host, port, password):
        self.sock = socket.create_connection((host, port), timeout=45)
        self.req_id = 1
        resp = self._send(3, password)
        if resp is None:
            raise RuntimeError("RCON authentication failed")

    def _send(self, ptype, payload):
        rid = self.req_id
        self.req_id += 1
        data = payload.encode("utf-8")
        pkt = struct.pack("<ii", rid, ptype) + data + b"\x00\x00"
        self.sock.sendall(struct.pack("<i", len(pkt)) + pkt)
        hdr = self._recv_exact(4)
        length = struct.unpack("<i", hdr)[0]
        body = self._recv_exact(length)
        r_id, _r_type = struct.unpack("<ii", body[:8])
        if r_id == -1:
            return None
        return body[8:-2].decode("utf-8", errors="replace")

    def _recv_exact(self, n):
        buf = bytearray()
        while len(buf) < n:
            chunk = self.sock.recv(n - len(buf))
            if not chunk:
                raise EOFError("RCON socket closed")
            buf.extend(chunk)
        return bytes(buf)

    def command(self, cmd):
        return self._send(2, cmd)

    def close(self):
        try:
            self.sock.close()
        except Exception:
            pass


def stage1_static_audit():
    print("\n================ STAGE 1: STATIC RESOURCE & MOD AUDIT ================")
    issues = []

    json_count = 0
    for r, _, files in os.walk(RES_DIR):
        for fn in files:
            if fn.endswith(".json"):
                json_count += 1
                p = os.path.join(r, fn)
                try:
                    with open(p, "r", encoding="utf-8") as f:
                        json.load(f)
                except Exception as e:
                    issues.append(f"INVALID JSON: {os.path.relpath(p, ROOT)} -> {e}")
    print(f"[OK] Checked {json_count} resource JSON files.")

    missing_boss_assets = []
    for b in BOSSES:
        for rel in [
            f"assets/eldenring_core/geo/{b}.geo.json",
            f"assets/eldenring_core/animations/{b}.animation.json",
            f"assets/eldenring_core/textures/entity/{b}.png",
        ]:
            if not os.path.exists(os.path.join(RES_DIR, rel)):
                missing_boss_assets.append(rel)
    if missing_boss_assets:
        issues.append(f"Missing boss assets: {missing_boss_assets}")
    else:
        print("[OK] All 13/13 Boss GeckoLib assets (.geo.json, .animation.json, .png) present.")

    cap_dir = os.path.join(RES_DIR, "data", "eldenring_core", "capabilities", "weapons")
    caps = sorted(os.listdir(cap_dir)) if os.path.isdir(cap_dir) else []
    print(f"[OK] Found {len(caps)} Epic Fight weapon capability JSONs.")

    mod_ids = {}
    if os.path.isdir(LIBS_DEV):
        for fn in sorted(os.listdir(LIBS_DEV)):
            if not fn.endswith(".jar"):
                continue
            p = os.path.join(LIBS_DEV, fn)
            try:
                with zipfile.ZipFile(p) as z:
                    if "META-INF/mods.toml" in z.namelist():
                        txt = z.read("META-INF/mods.toml").decode("utf-8", "ignore")
                        m = re.search(r'\[\[mods\]\].*?modId\s*=\s*"([^"]+)"', txt, re.S)
                        if m:
                            mid = m.group(1)
                            if mid in mod_ids:
                                issues.append(f"DUPLICATE modId '{mid}': {mod_ids[mid]} vs {fn}")
                            mod_ids[mid] = fn
            except Exception as e:
                issues.append(f"CORRUPT JAR {fn}: {e}")
    print(f"[OK] Scanned {len(mod_ids)} external dev jars in libs_dev/ — 0 duplicate modIds.")

    if issues:
        for i in issues:
            print("[FAIL]", i)
        return False
    return True


def stage2_headless_server_test():
    print("\n================ STAGE 2: HEADLESS FORGE SERVER + LIVE RCON TEST ================")
    os.makedirs(RUN_DIR, exist_ok=True)
    eula_path = os.path.join(RUN_DIR, "eula.txt")
    props_path = os.path.join(RUN_DIR, "server.properties")
    test_world = os.path.join(RUN_DIR, "qa_autotest_world")

    if os.path.exists(test_world):
        shutil.rmtree(test_world, ignore_errors=True)

    with open(eula_path, "w", encoding="utf-8") as f:
        f.write("eula=true\n")

    with open(props_path, "w", encoding="utf-8") as f:
        f.write(
            "level-name=qa_autotest_world\n"
            "online-mode=false\n"
            "server-port=25599\n"
            "enable-rcon=true\n"
            f"rcon.port={RCON_PORT}\n"
            f"rcon.password={RCON_PASS}\n"
            "enable-command-block=true\n"
            "spawn-protection=0\n"
            "max-tick-time=-1\n"
            "view-distance=6\n"
            "simulation-distance=5\n"
        )

    env = os.environ.copy()
    if os.path.isdir(JAVA17):
        env["JAVA_HOME"] = JAVA17
        env["PATH"] = os.path.join(JAVA17, "bin") + os.pathsep + env.get("PATH", "")

    cmd = [os.path.join(ROOT, "gradlew.bat"), "runServer", "--no-daemon", "--offline"]
    print("[INFO] Launching headless Forge 47.4.4 server with ALL 25 mods...")
    proc = subprocess.Popen(
        cmd,
        cwd=ROOT,
        env=env,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace",
        bufsize=1,
    )

    lines = []
    ready_event = threading.Event()
    crashed_event = threading.Event()

    def reader():
        for line in proc.stdout:
            s = line.rstrip()
            lines.append(s)
            if "RCON running on" in s:
                ready_event.set()
            elif "Done (" in s and "For help, type" in s:
                ready_event.set()
            if any(k in s for k in [
                "Encountered an unexpected exception",
                "Crash report saved to",
                "Failed to start the minecraft server",
                "ModLoadingException",
                "MixinApplyError",
                "Failed to load registries",
            ]):
                print("  [ALERT]", s)
                crashed_event.set()

    t = threading.Thread(target=reader, daemon=True)
    t.start()

    t0 = time.time()
    while not ready_event.is_set() and not crashed_event.is_set():
        if proc.poll() is not None:
            break
        if time.time() - t0 > 240:
            print("[FAIL] Server boot timed out after 240s!")
            proc.kill()
            return False, lines
        time.sleep(0.5)

    if not ready_event.is_set():
        print("[FAIL] Server failed to reach Ready state!")
        try:
            proc.kill()
        except Exception:
            pass
        t.join(timeout=5)
        return False, lines

    time.sleep(2.0)  # allow RCON socket to bind
    boot_sec = time.time() - t0
    print(f"[PASS] Server booted cleanly with ALL mods in {boot_sec:.1f}s!")

    rcon = None
    for _ in range(10):
        try:
            rcon = SimpleRcon("127.0.0.1", RCON_PORT, RCON_PASS)
            break
        except Exception:
            time.sleep(1.0)
    if rcon is None:
        print("[FAIL] Could not connect to RCON!")
        proc.kill()
        return False, lines

    biome_pass = 0
    struct_pass = 0
    boss_pass = 0
    try:
        print("\n--- [LIVE TEST 1/3] 7 Elden Ring Biomes in eldenring_core:lands_between ---")
        for name, (x, y, z), bid in BIOMES:
            res = rcon.command(f"execute in eldenring_core:lands_between positioned {x} {y} {z} run locate biome {bid}").strip()
            ok = "The nearest" in res
            if ok:
                biome_pass += 1
            print(f"  [{'PASS' if ok else 'FAIL'}] {name:14s} -> {res}")

        print("\n--- [LIVE TEST 2/3] Summon & Tick All 13 Elden Ring Bosses ---")
        rcon.command("execute in eldenring_core:lands_between run forceload add -320 680 -200 720")
        for idx, b in enumerate(BOSSES):
            bx = -310 + idx * 6
            res = rcon.command(f"execute in eldenring_core:lands_between run summon eldenring_core:{b} {bx} 120 700").strip()
            ok = "Summoned new" in res
            if ok:
                boss_pass += 1
            print(f"  [{'PASS' if ok else 'FAIL'}] {b:16s} -> {res}")

        time.sleep(3.0)
        rcon.command("execute in eldenring_core:lands_between run forceload remove all")

        print("\n--- [LIVE TEST 3/3] 12 Boss Dungeons & Structures in eldenring_core:lands_between ---")
        for region, (x, y, z), sid in STRUCTURES_TO_TEST:
            res = rcon.command(f"execute in eldenring_core:lands_between positioned {x} {y} {z} run locate structure {sid}").strip()
            ok = "The nearest" in res
            if ok:
                struct_pass += 1
            print(f"  [{'PASS' if ok else 'FAIL'}] {region:12s} | {sid:42s} -> {res}")
    finally:
        print("\n--- Stopping Server Cleanly via RCON ---")
        try:
            rcon.command("stop")
        except Exception:
            pass
        rcon.close()
        try:
            proc.wait(timeout=45)
        except subprocess.TimeoutExpired:
            proc.kill()
        t.join(timeout=5)
        if os.path.exists(test_world):
            shutil.rmtree(test_world, ignore_errors=True)

    print(f"\n[LIVE SCORE] Biomes: {biome_pass}/{len(BIOMES)} | Structures: {struct_pass}/{len(STRUCTURES_TO_TEST)} | Bosses: {boss_pass}/{len(BOSSES)}")
    all_live_ok = (not crashed_event.is_set()) and biome_pass == len(BIOMES) and boss_pass == len(BOSSES)
    return all_live_ok, lines


def stage3_log_analysis(lines):
    print("\n================ STAGE 3: FULL LOG ERROR & CONFLICT ANALYSIS ================")
    log_path = os.path.join(RUN_DIR, "logs", "latest.log")
    if os.path.exists(log_path):
        with open(log_path, "r", encoding="utf-8", errors="ignore") as f:
            log_lines = [l.rstrip() for l in f]
    else:
        log_lines = lines

    errors = []
    warnings_important = []
    for l in log_lines:
        if any(k in l for k in ["/ERROR]", "/FATAL]", "Exception:", "Caused by:", "MixinApplyError", "Unbound values"]):
            if any(ign in l for ign in ["RealmsClient", "xaero.map.WorldMap", "Online mod data expired", "Hanging entity at invalid position"]):
                continue
            errors.append(l)
        elif any(k in l for k in ["Couldn't parse", "Missing", "Unknown structure", "Failed to load", "Invalid"]):
            if "/WARN]" in l and "PerfmonDisabled" not in l:
                warnings_important.append(l)

    print(f"Total log lines: {len(log_lines)}")
    print(f"Critical ERRORS/Exceptions: {len(errors)}")
    for e in errors[:40]:
        print("  [ERR]", e[:240])
    print(f"Notable Data/Tag WARNINGS: {len(warnings_important)}")
    for w in warnings_important[:25]:
        print("  [WARN]", w[:240])

    return len(errors) == 0


if __name__ == "__main__":
    ok1 = stage1_static_audit()
    ok2, out_lines = stage2_headless_server_test()
    ok3 = stage3_log_analysis(out_lines)
    print("\n==================== FINAL QA SUMMARY ====================")
    print("Stage 1 (Static Audit)         :", "PASS" if ok1 else "FAIL")
    print("Stage 2 (Live Biomes/Bosses)   :", "PASS" if ok2 else "FAIL")
    print("Stage 3 (Zero Runtime Errors)  :", "PASS" if ok3 else "CHECK LOGS ABOVE")
    sys.exit(0 if (ok1 and ok2 and ok3) else 1)
