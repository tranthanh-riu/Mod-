# Install once in the dedicated wave3_4 dimension.
execute if data storage eldenring_core:wave3_4 built run tellraw @s {"translate":"message.eldenring_core.wave3_4.already_built"}
execute unless data storage eldenring_core:wave3_4 built run tag @s[type=minecraft:player] add eldenring_core.wave3_4_installer
execute unless data storage eldenring_core:wave3_4 built in eldenring_core:wave3_4 run function eldenring_core:wave3_4/setup/prepare
