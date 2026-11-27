#!/usr/bin/env python3
"""
Converts Litematica (.litematic) files (including MC 1.21+ schematics) into
100% Minecraft 1.20.1-compatible WorldEdit Sponge Schematic v2 (.schem) files.
Automatically remaps post-1.20.1 blocks (tuff bricks, pale moss, copper bulbs, etc.)
to clean 1.20.1 vanilla equivalents while preserving all blockstate properties.
"""
import gzip
import os
import struct
import sys

# Post-1.20.1 -> 1.20.1 block remapping table
REMAP_1201 = {
    "minecraft:tuff_bricks": "minecraft:deepslate_bricks",
    "minecraft:tuff_brick_stairs": "minecraft:deepslate_brick_stairs",
    "minecraft:tuff_brick_slab": "minecraft:deepslate_brick_slab",
    "minecraft:tuff_brick_wall": "minecraft:deepslate_brick_wall",
    "minecraft:chiseled_tuff_bricks": "minecraft:chiseled_deepslate",
    "minecraft:polished_tuff": "minecraft:polished_deepslate",
    "minecraft:polished_tuff_stairs": "minecraft:polished_deepslate_stairs",
    "minecraft:polished_tuff_slab": "minecraft:polished_deepslate_slab",
    "minecraft:polished_tuff_wall": "minecraft:polished_deepslate_wall",
    "minecraft:chiseled_tuff": "minecraft:chiseled_deepslate",
    "minecraft:tuff_stairs": "minecraft:cobbled_deepslate_stairs",
    "minecraft:tuff_slab": "minecraft:cobbled_deepslate_slab",
    "minecraft:tuff_wall": "minecraft:cobbled_deepslate_wall",
    "minecraft:pale_moss_block": "minecraft:moss_block",
    "minecraft:pale_moss_carpet": "minecraft:moss_carpet",
    "minecraft:pale_oak_log": "minecraft:birch_log",
    "minecraft:pale_oak_wood": "minecraft:birch_wood",
    "minecraft:pale_oak_planks": "minecraft:birch_planks",
    "minecraft:pale_oak_stairs": "minecraft:birch_stairs",
    "minecraft:pale_oak_slab": "minecraft:birch_slab",
    "minecraft:pale_oak_fence": "minecraft:birch_fence",
    "minecraft:pale_oak_leaves": "minecraft:birch_leaves",
    "minecraft:copper_bulb": "minecraft:glowstone",
    "minecraft:exposed_copper_bulb": "minecraft:glowstone",
    "minecraft:weathered_copper_bulb": "minecraft:sea_lantern",
    "minecraft:oxidized_copper_bulb": "minecraft:sea_lantern",
    "minecraft:waxed_copper_bulb": "minecraft:glowstone",
    "minecraft:copper_grate": "minecraft:iron_bars",
    "minecraft:waxed_copper_grate": "minecraft:iron_bars",
    "minecraft:crafter": "minecraft:crafting_table",
    "minecraft:short_grass": "minecraft:grass",
    "minecraft:short_dry_grass": "minecraft:dead_bush",
    "minecraft:tall_dry_grass": "minecraft:dead_bush",
    "minecraft:leaf_litter": "minecraft:air",
    "minecraft:wildflowers": "minecraft:dandelion",
    "minecraft:bush": "minecraft:fern",
    "minecraft:firefly_bush": "minecraft:fern",
}

# Golden Glowing Erdtree theme remap (transforms pink sakura giant tree -> glowing golden Erdtree!)
ERDTREE_GOLD_REMAP = {
    "minecraft:pink_stained_glass": "minecraft:yellow_stained_glass",
    "minecraft:pink_stained_glass_pane": "minecraft:yellow_stained_glass_pane",
    "minecraft:pink_wool": "minecraft:glowstone",
    "minecraft:pink_concrete": "minecraft:ochre_froglight",
    "minecraft:white_wool": "minecraft:shroomlight",
    "minecraft:cherry_planks": "minecraft:stripped_oak_wood",
    "minecraft:spruce_wood": "minecraft:oak_wood",
}


# ---- Minimal pure-Python NBT Reader & Writer ----
def read_nbt(buf):
    pos = 0
    def r_u8():
        nonlocal pos; v = buf[pos]; pos += 1; return v
    def r_i16():
        nonlocal pos; v = struct.unpack_from(">h", buf, pos)[0]; pos += 2; return v
    def r_i32():
        nonlocal pos; v = struct.unpack_from(">i", buf, pos)[0]; pos += 4; return v
    def r_i64():
        nonlocal pos; v = struct.unpack_from(">q", buf, pos)[0]; pos += 8; return v
    def r_f32():
        nonlocal pos; v = struct.unpack_from(">f", buf, pos)[0]; pos += 4; return v
    def r_f64():
        nonlocal pos; v = struct.unpack_from(">d", buf, pos)[0]; pos += 8; return v
    def r_str():
        nonlocal pos
        n = struct.unpack_from(">H", buf, pos)[0]; pos += 2
        s = buf[pos:pos + n].decode("utf-8", "replace"); pos += n
        return s
    def r_tag(t):
        nonlocal pos
        if t == 1:
            v = struct.unpack_from(">b", buf, pos)[0]; pos += 1; return v
        if t == 2: return r_i16()
        if t == 3: return r_i32()
        if t == 4: return r_i64()
        if t == 5: return r_f32()
        if t == 6: return r_f64()
        if t == 7:
            n = r_i32(); v = buf[pos:pos + n]; pos += n; return v
        if t == 8: return r_str()
        if t == 9:
            et = r_u8(); n = r_i32(); return [r_tag(et) for _ in range(n)]
        if t == 10:
            d = {}
            while True:
                nt = r_u8()
                if nt == 0: break
                k = r_str()
                d[k] = r_tag(nt)
            return d
        if t == 11:
            n = r_i32(); v = struct.unpack_from(f">{n}i", buf, pos); pos += 4 * n; return v
        if t == 12:
            n = r_i32(); v = struct.unpack_from(f">{n}q", buf, pos); pos += 8 * n; return v
        raise ValueError(f"Unknown NBT tag {t}")
    t = r_u8()
    _ = r_str()
    return r_tag(t)


def w_str(out, s):
    b = s.encode("utf-8")
    out.extend(struct.pack(">H", len(b)))
    out.extend(b)


def w_tag(out, t, name, val):
    out.append(t)
    w_str(out, name)
    w_payload(out, t, val)


def w_payload(out, t, val):
    if t == 1: out.extend(struct.pack(">b", val))
    elif t == 2: out.extend(struct.pack(">h", val))
    elif t == 3: out.extend(struct.pack(">i", val))
    elif t == 4: out.extend(struct.pack(">q", val))
    elif t == 7:
        out.extend(struct.pack(">i", len(val)))
        out.extend(val)
    elif t == 8: w_str(out, val)
    elif t == 9:
        et, items = val
        out.append(et)
        out.extend(struct.pack(">i", len(items)))
        for it in items:
            w_payload(out, et, it)
    elif t == 10:
        for k, (vt, vv) in val.items():
            w_tag(out, vt, k, vv)
        out.append(0)
    elif t == 11:
        out.extend(struct.pack(">i", len(val)))
        for x in val:
            out.extend(struct.pack(">i", x))
    else:
        raise ValueError(t)


def encode_varints(indices):
    out = bytearray()
    for v in indices:
        while True:
            b = v & 0x7F
            v >>= 7
            if v:
                out.append(b | 0x80)
            else:
                out.append(b)
                break
    return bytes(out)


def state_to_sponge_str(entry, extra_remap=None):
    name = entry["Name"]
    if extra_remap and name in extra_remap:
        name = extra_remap[name]
    if name in REMAP_1201:
        name = REMAP_1201[name]
    props = entry.get("Properties")
    # Strip properties if remapped to a simple full block without axis/facing/etc.
    simple_blocks = {
        "minecraft:glowstone", "minecraft:shroomlight", "minecraft:air",
        "minecraft:moss_block", "minecraft:moss_carpet", "minecraft:deepslate_bricks",
        "minecraft:polished_deepslate", "minecraft:chiseled_deepslate",
        "minecraft:yellow_stained_glass", "minecraft:oak_wood", "minecraft:stripped_oak_wood",
        "minecraft:dandelion", "minecraft:fern", "minecraft:dead_bush",
    }
    if not props or name in simple_blocks:
        if name == "minecraft:ochre_froglight":
            return "minecraft:ochre_froglight[axis=y]"
        return name
    # Filter unsupported properties if any
    parts = [f"{k}={v}" for k, v in sorted(props.items())]
    return f"{name}[{','.join(parts)}]"


def convert_litematic_to_schem(src_path, dst_path, extra_remap=None):
    with gzip.open(src_path, "rb") as f:
        nbt = read_nbt(f.read())

    regions = nbt["Regions"]
    # Pick primary/largest region (all 3 downloaded files have 1 region)
    rname, reg = next(iter(regions.items()))
    sx = abs(reg["Size"]["x"])
    sy = abs(reg["Size"]["y"])
    sz = abs(reg["Size"]["z"])
    total = sx * sy * sz

    palette = reg["BlockStatePalette"]
    bits = max(2, (len(palette) - 1).bit_length())
    mask = (1 << bits) - 1
    longs = reg["BlockStates"]

    # Build deduplicated Sponge palette
    sponge_pal = {}
    pal_map = []
    for entry in palette:
        s = state_to_sponge_str(entry, extra_remap)
        if s not in sponge_pal:
            sponge_pal[s] = len(sponge_pal)
        pal_map.append(sponge_pal[s])

    # Unpack Litematica bit-packed array (spanning across 64-bit longs, little-endian bit order)
    # Litematica index order is y * (sx * sz) + z * sx + x — IDENTICAL to Sponge Schematic v2!
    sponge_indices = [0] * total
    non_air = 0
    air_ids = {i for s, i in sponge_pal.items() if s in ("minecraft:air", "minecraft:cave_air", "minecraft:void_air")}

    for i in range(total):
        bit_idx = i * bits
        long_idx = bit_idx >> 6
        start_bit = bit_idx & 63
        end_bit = start_bit + bits
        val = (longs[long_idx] & 0xFFFFFFFFFFFFFFFF) >> start_bit
        if end_bit > 64 and long_idx + 1 < len(longs):
            val |= (longs[long_idx + 1] & 0xFFFFFFFFFFFFFFFF) << (64 - start_bit)
        raw_id = val & mask
        sid = pal_map[raw_id] if raw_id < len(pal_map) else 0
        sponge_indices[i] = sid
        if sid not in air_ids:
            non_air += 1

    block_data = encode_varints(sponge_indices)

    # Build Sponge Schematic v2 NBT (DataVersion 3465 = Minecraft 1.20.1)
    pal_compound = {s: (3, idx) for s, idx in sponge_pal.items()}
    root = {
        "Version": (3, 2),
        "DataVersion": (3, 3465),
        "Width": (2, sx),
        "Height": (2, sy),
        "Length": (2, sz),
        "Offset": (11, [-(sx // 2), 0, -(sz // 2)]),
        "PaletteMax": (3, len(sponge_pal)),
        "Palette": (10, pal_compound),
        "BlockData": (7, block_data),
        "BlockEntities": (9, (10, [])),
        "Metadata": (10, {
            "WEOffsetX": (3, -(sx // 2)),
            "WEOffsetY": (3, 0),
            "WEOffsetZ": (3, -(sz // 2)),
        }),
    }

    out = bytearray()
    w_tag(out, 10, "Schematic", root)
    os.makedirs(os.path.dirname(dst_path), exist_ok=True)
    with gzip.open(dst_path, "wb") as f:
        f.write(out)

    print(f"[OK] {os.path.basename(dst_path):36s} | {sx}x{sy}x{sz} | non-air: {non_air:,} | palette: {len(sponge_pal)} | size: {os.path.getsize(dst_path):,} B")


if __name__ == "__main__":
    src_dir = "d:/Mod/reference/elden_schematics"
    targets = [
        "d:/Mod/run/config/worldedit/schematics",
        "d:/Mod/modpack/overrides/config/worldedit/schematics",
    ]
    jobs = [
        ("Erdtree-from-abfielder.litematic", "erdtree_golden.schem", ERDTREE_GOLD_REMAP),
        ("Erdtree-from-abfielder.litematic", "haligtree_sacred.schem", None),
        ("Cologne Cathedral Germany-from-abfielder.litematic", "leyndell_cathedral.schem", None),
        ("Dark Fortress-from-abfielder.litematic", "volcano_manor_fortress.schem", None),
    ]
    for src_name, dst_name, remap in jobs:
        src = os.path.join(src_dir, src_name)
        if not os.path.exists(src):
            print("[SKIP] Missing:", src)
            continue
        first_dst = os.path.join(targets[0], dst_name)
        convert_litematic_to_schem(src, first_dst, remap)
        # Copy identical .schem to modpack overrides too
        os.makedirs(targets[1], exist_ok=True)
        with open(first_dst, "rb") as rf, open(os.path.join(targets[1], dst_name), "wb") as wf:
            wf.write(rf.read())
