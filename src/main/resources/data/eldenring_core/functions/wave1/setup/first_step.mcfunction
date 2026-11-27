# Wind-beaten headland and trail east.
fill -19 89 -10 9 93 10 minecraft:stone
fill -22 92 -7 -20 95 7 minecraft:andesite
fill -17 94 -12 8 95 12 minecraft:dirt
fill -17 96 -12 8 96 12 minecraft:grass_block
fill -12 96 -3 14 96 3 minecraft:coarse_dirt
fill -10 96 -1 14 96 1 minecraft:dirt_path
fill -16 91 -14 3 93 13 minecraft:stone
fill -20 94 -8 -18 97 8 minecraft:mossy_cobblestone
fill -16 97 -10 -12 97 -10 minecraft:cobblestone_wall
fill -16 97 10 -12 97 10 minecraft:cobblestone_wall
setblock -10 97 -4 minecraft:cobblestone_wall
setblock -10 98 -4 minecraft:lantern
setblock -10 97 4 minecraft:cobblestone_wall
setblock -10 98 4 minecraft:lantern
setblock -7 97 2 eldenring_core:site_of_grace
grace name -7 97 2 The First Step
setblock -7 97 -3 minecraft:barrel[facing=up]{Items:[{Slot:0b,id:"eldenring_core:flask_of_crimson_tears",Count:1b},{Slot:1b,id:"eldenring_core:flask_of_cerulean_tears",Count:1b},{Slot:2b,id:"minecraft:bread",Count:8b}]}
setblock -5 97 3 minecraft:oak_sign[rotation=8]{front_text:{messages:['{"text":"The First Step"}','{"text":"East: Sentinel"}','{"text":"South fork: forge"}','{"text":""}']}}
