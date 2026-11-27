function eldenring_core:wave2/setup/lake_shore
function eldenring_core:wave2/setup/liurnia_lake
function eldenring_core:wave2/setup/raya_lucaria
function eldenring_core:wave2/setup/caelid_waste
function eldenring_core:wave2/setup/radahn_dunes
data modify storage eldenring_core:wave2 built set value 1b
forceload remove -24 -28 203 28
tellraw @a[tag=eldenring_core.wave2_installer] {"translate":"message.eldenring_core.wave2.ready"}
tag @a[tag=eldenring_core.wave2_installer] remove eldenring_core.wave2_installer
