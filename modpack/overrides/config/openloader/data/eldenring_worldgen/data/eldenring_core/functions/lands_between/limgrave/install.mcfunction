# Build the Limgrave sites into the Lands Between once (needs the modpack: Terralith + Open Loader).
execute if data storage eldenring_core:limgrave built run tellraw @s {"text":"Limgrave Ä‘Ã£ Ä‘Æ°á»£c dá»±ng. DÃ¹ng /function eldenring_core:lands_between/limgrave/enter Ä‘á»ƒ vÃ o.","color":"gold"}
execute unless data storage eldenring_core:limgrave built run tag @s[type=minecraft:player] add eldenring_core.limgrave_installer
execute unless data storage eldenring_core:limgrave built in eldenring_core:lands_between run function eldenring_core:lands_between/limgrave/prepare
