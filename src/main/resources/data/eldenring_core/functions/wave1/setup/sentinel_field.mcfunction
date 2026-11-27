# Broad grassy arena with broken walls, an oak stand and a trail through the east side.
fill 10 90 -20 46 95 20 minecraft:stone
fill 10 95 -20 46 95 20 minecraft:dirt
fill 10 96 -20 46 96 20 minecraft:grass_block
fill 10 96 -1 50 96 1 minecraft:dirt_path
fill 8 94 -3 11 96 3 minecraft:coarse_dirt
fill 45 93 -5 59 96 5 minecraft:cobblestone
fill 46 96 -2 59 96 2 minecraft:dirt_path
fill 44 95 2 49 96 19 minecraft:coarse_dirt
fill 45 96 2 47 96 21 minecraft:dirt_path
fill 13 97 -17 24 97 -17 minecraft:cobblestone_wall
fill 32 97 -17 43 97 -17 minecraft:mossy_cobblestone_wall
fill 13 97 17 22 97 17 minecraft:mossy_cobblestone_wall
fill 35 97 17 42 97 17 minecraft:cobblestone_wall
fill 17 97 -6 17 101 -6 minecraft:oak_log
fill 15 101 -8 19 102 -4 minecraft:oak_leaves[persistent=true] replace minecraft:air
fill 16 103 -7 18 103 -5 minecraft:oak_leaves[persistent=true]
fill 39 97 10 39 101 10 minecraft:oak_log
fill 37 101 8 41 102 12 minecraft:oak_leaves[persistent=true] replace minecraft:air
fill 38 103 9 40 103 11 minecraft:oak_leaves[persistent=true]
setblock 27 96 0 eldenring_core:boss_arena_controller{BossId:"eldenring_core:tree_sentinel",TriggerRadius:8.0d,ResetRadius:17.0d}
setblock 45 97 4 minecraft:oak_sign[rotation=4]{front_text:{messages:['{"text":"Church of Elleh"}','{"text":"Follow the lanterns"}','{"text":""}','{"text":""}']}}
setblock 44 97 7 minecraft:cobblestone_wall
setblock 44 98 7 minecraft:lantern
setblock 48 97 15 minecraft:cobblestone_wall
setblock 48 98 15 minecraft:lantern
