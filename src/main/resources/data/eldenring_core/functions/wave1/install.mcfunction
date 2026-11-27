# Install once in the dedicated Wave 1 dimension.
execute if data storage eldenring_core:wave1 built run tellraw @s {"translate":"message.eldenring_core.wave1.already_built"}
execute unless data storage eldenring_core:wave1 built run tag @s[type=minecraft:player] add eldenring_core.wave1_installer
execute unless data storage eldenring_core:wave1 built in eldenring_core:wave1 run function eldenring_core:wave1/setup/prepare
