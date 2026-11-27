# Golden fields below the capital outer wall.
fill -24 90 -20 19 95 20 minecraft:stone
fill -22 96 -18 19 96 18 minecraft:grass_block
fill -12 96 -2 19 96 2 minecraft:smooth_sandstone
fill -18 97 -16 -2 97 -6 minecraft:dandelion replace minecraft:air
fill -18 97 6 -2 97 16 minecraft:dandelion replace minecraft:air
fill 4 97 -16 14 97 -8 minecraft:sunflower[half=lower]
fill 4 98 -16 14 98 -8 minecraft:sunflower[half=upper]
fill 17 97 -20 19 112 20 minecraft:smooth_sandstone
fill 17 97 -3 19 104 3 minecraft:air
fill 17 105 -3 19 106 3 minecraft:chiseled_sandstone
fill 16 113 -20 20 113 20 minecraft:cut_sandstone_slab[type=bottom]
setblock -10 97 -4 minecraft:sandstone_wall
setblock -10 98 -4 minecraft:lantern
setblock -10 97 4 minecraft:sandstone_wall
setblock -10 98 4 minecraft:lantern
setblock -7 97 2 eldenring_core:site_of_grace
grace name -7 97 2 Leyndell Outer Wall
setblock -5 97 3 minecraft:oak_sign[rotation=4]{front_text:{messages:['{"text":"Leyndell"}','{"text":"Royal Capital"}','{"text":"The Erdtree east"}','{"text":""}']}}
