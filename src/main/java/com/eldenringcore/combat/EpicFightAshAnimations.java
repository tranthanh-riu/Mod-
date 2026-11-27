package com.eldenringcore.combat;

import com.eldenringcore.EldenRingCore;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/**
 * Plays an Epic Fight body animation for each Ash of War, so the skill looks like a real move
 * instead of a vanilla arm swing. Animations are Epic Fight's own (used through its API).
 */
public final class EpicFightAshAnimations {
    private EpicFightAshAnimations() {
    }

    /** True when the player is in Epic Fight's battle mode (right-click = Guard there). */
    public static boolean isBattleMode(Player player) {
        PlayerPatch<?> patch = EpicFightCapabilities.getPlayerPatch(player);
        return patch != null && patch.isEpicFightMode();
    }

    /** Animation id for an Ash of War; comboStage is used by multi-stage skills such as Corpse Piler. */
    static String animationFor(AshOfWarType type, int comboStage, boolean variant) {
        return switch (type) {
            // Rivers of Blood: three spinning blood cuts, one per press
            case CORPSE_PILER -> switch (comboStage) {
                case 1 -> "epicfight:biped/skill/blade_rush_combo2";
                case 2 -> "epicfight:biped/skill/blade_rush_combo3";
                default -> "epicfight:biped/skill/blade_rush_combo1";
            };
            // Moonveil: quick draw from the sheath; sneaking gives the lunging draw
            case TRANSIENT_MOONLIGHT -> variant ? "epicfight:biped/skill/battojutsu_dash" : "epicfight:biped/skill/battojutsu";
            // Dark Moon Greatsword: two-handed overhead blow that launches the wave
            case MOONLIGHT_WAVE -> "epicfight:biped/skill/greatsword_slam";
            // Hand of Malenia: whirling flurry
            case WATERFOWL_DANCE -> "epicfight:biped/skill/dancing_edge";
            // Blasphemous Blade: wide sweeping cut that throws the flames
            case TAKERS_FLAMES -> "epicfight:biped/skill/sweeping_edge";
            // Starscourge Greatsword: raise the blade and call the stars
            case STARCALLER_CRY -> "epicfight:biped/skill/revelation_twohand";
            // Maliketh's Black Blade: spinning cleave of black flame
            case DESTINED_DEATH -> "epicfight:biped/skill/steel_whirlwind";
            // Bloodhound's Fang: evasive backflip
            case BLOODHOUNDS_FINESSE -> "epicfight:biped/skill/phantom_ascent_backward";
            // Golden Halberd: raise the weapon in an oath
            case GOLDEN_VOW -> "epicfight:biped/skill/everlasting_allegiance_call";
            // Bolt of Gransax: hurl the lightning spear
            case ANCIENT_LIGHTNING_SPEAR -> "epicfight:biped/combat/javelin_throw_mid";
            default -> null;
        };
    }

    /** Server side: play the Ash of War animation on this player and sync it to everyone nearby. */
    public static void play(Player player, AshOfWarType type, int comboStage, boolean variant) {
        String id = animationFor(type, comboStage, variant);
        if (id == null) return;
        ServerPlayerPatch patch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (patch == null) return;
        try {
            AnimationManager.AnimationAccessor<StaticAnimation> anim = AnimationManager.byKey(id);
            if (anim != null) {
                patch.playAnimationSynchronized(anim, 0.0F);
            }
        } catch (RuntimeException e) {
            EldenRingCore.LOGGER.warn("Could not play Epic Fight animation {} for {}", id, type, e);
        }
    }
}
