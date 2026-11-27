function eldenring_core:wave3_4/setup/outer_wall
function eldenring_core:wave3_4/setup/leyndell
function eldenring_core:wave3_4/setup/erdtree_sanctuary
function eldenring_core:wave3_4/setup/elden_throne
function eldenring_core:wave3_4/setup/erdtree
function eldenring_core:wave3_4/setup/haligtree
data modify storage eldenring_core:wave3_4 built set value 1b
forceload remove -24 -48 330 48
tellraw @a[tag=eldenring_core.wave3_4_installer] {"translate":"message.eldenring_core.wave3_4.ready"}
tag @a[tag=eldenring_core.wave3_4_installer] remove eldenring_core.wave3_4_installer
