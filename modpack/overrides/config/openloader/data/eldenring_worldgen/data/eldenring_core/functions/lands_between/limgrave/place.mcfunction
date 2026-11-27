function eldenring_core:lands_between/limgrave/gatefront
function eldenring_core:lands_between/limgrave/castle
data modify storage eldenring_core:limgrave built set value 1b
forceload remove -496 1536 -406 1609
forceload remove -484 539 -376 581
tellraw @a[tag=eldenring_core.limgrave_installer] {"text":"Limgrave Ä‘Ã£ sáºµn sÃ ng. DÃ¹ng /function eldenring_core:lands_between/limgrave/enter Ä‘á»ƒ vÃ o.","color":"gold"}
tag @a[tag=eldenring_core.limgrave_installer] remove eldenring_core.limgrave_installer
