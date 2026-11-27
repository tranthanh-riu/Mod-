function eldenring_core:wave1/setup/first_step
function eldenring_core:wave1/setup/sentinel_field
function eldenring_core:wave1/setup/elleh
function eldenring_core:wave1/setup/stormveil_bridge
function eldenring_core:wave1/setup/stormveil_keep
data modify storage eldenring_core:wave1 built set value 1b
forceload remove -24 -24 157 49
tellraw @a[tag=eldenring_core.wave1_installer] {"translate":"message.eldenring_core.wave1.ready"}
tag @a[tag=eldenring_core.wave1_installer] remove eldenring_core.wave1_installer
