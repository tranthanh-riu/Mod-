package com.eldenringcore.combat;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Glow ("bloom") layers for Ash of War, drawn with vanilla particles so they work without shaders:
 * a full-bright core (end rod, sparks, flames), a wide soft colour halo (large dust) around it,
 * and a flash where the blow lands. Timed to the swing of the Epic Fight animation.
 */
public final class AshVfx {
    private AshVfx() {
    }

    private record Theme(Vector3f halo, Vector3f core, ParticleOptions glow, ParticleOptions accent) {
    }

    private static Vector3f rgb(float r, float g, float b) {
        return new Vector3f(r, g, b);
    }

    private static final Theme MOON = new Theme(rgb(0.35f, 0.65f, 1.0f), rgb(0.85f, 0.95f, 1.0f), ParticleTypes.END_ROD, ParticleTypes.SOUL_FIRE_FLAME);
    private static final Theme DARK_MOON = new Theme(rgb(0.45f, 0.75f, 1.0f), rgb(0.95f, 1.0f, 1.0f), ParticleTypes.END_ROD, ParticleTypes.SNOWFLAKE);
    private static final Theme BLOODFLAME = new Theme(rgb(0.75f, 0.02f, 0.04f), rgb(1.0f, 0.35f, 0.25f), ParticleTypes.FLAME, ParticleTypes.CRIMSON_SPORE);
    private static final Theme SCARLET = new Theme(rgb(0.85f, 0.15f, 0.25f), rgb(1.0f, 0.75f, 0.8f), ParticleTypes.END_ROD, ParticleTypes.CHERRY_LEAVES);
    private static final Theme FIRE = new Theme(rgb(1.0f, 0.45f, 0.05f), rgb(1.0f, 0.85f, 0.4f), ParticleTypes.FLAME, ParticleTypes.LAVA);
    private static final Theme GRAVITY = new Theme(rgb(0.45f, 0.15f, 0.85f), rgb(0.85f, 0.7f, 1.0f), ParticleTypes.END_ROD, ParticleTypes.REVERSE_PORTAL);
    private static final Theme DESTINED = new Theme(rgb(0.55f, 0.0f, 0.08f), rgb(0.12f, 0.0f, 0.02f), ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.LARGE_SMOKE);
    private static final Theme GOLD = new Theme(rgb(1.0f, 0.78f, 0.2f), rgb(1.0f, 0.97f, 0.7f), ParticleTypes.END_ROD, ParticleTypes.WAX_ON);
    private static final Theme RED_LIGHTNING = new Theme(rgb(1.0f, 0.2f, 0.15f), rgb(1.0f, 0.85f, 0.8f), ParticleTypes.ELECTRIC_SPARK, ParticleTypes.CRIMSON_SPORE);

    /** Plays the glow for an Ash of War, delayed to land with the swing of its animation. */
    public static void play(ServerLevel level, Player player, AshOfWarType type, int comboStage, boolean variant) {
        Vec3 f = flatForward(player);
        switch (type) {
            case TRANSIENT_MOONLIGHT -> later(level, variant ? 9 : 6, () -> {
                if (variant) line(level, player, f, 9.0, MOON, 1.0); else arc(level, player, f, 4.2, 150, MOON, 0.0);
            });
            case MOONLIGHT_WAVE -> later(level, 8, () -> line(level, player, f, 10.0, DARK_MOON, 1.3));
            case CORPSE_PILER -> later(level, 4, () -> arc(level, player, f, 3.6, 300, BLOODFLAME, comboStage * 0.6));
            case WATERFOWL_DANCE -> {
                later(level, 4, () -> ring(level, player.position().add(0, 1.0, 0), 3.2, SCARLET));
                later(level, 9, () -> ring(level, player.position().add(0, 1.4, 0), 4.0, SCARLET));
                later(level, 14, () -> ring(level, player.position().add(0, 0.8, 0), 4.6, SCARLET));
            }
            case TAKERS_FLAMES -> later(level, 6, () -> arc(level, player, f, 5.0, 170, FIRE, 0.0));
            case STARCALLER_CRY -> {
                later(level, 6, () -> ring(level, player.position().add(0, 0.2, 0), 6.0, GRAVITY));
                later(level, 14, () -> flash(level, player.position().add(0, 1.2, 0), GRAVITY, 3));
            }
            case DESTINED_DEATH -> later(level, 7, () -> arc(level, player, f, 4.6, 360, DESTINED, 0.0));
            case BLOODHOUNDS_FINESSE -> later(level, 3, () -> arc(level, player, f, 3.0, 200, BLOODFLAME, 1.2));
            case GOLDEN_VOW -> {
                later(level, 6, () -> ring(level, player.position().add(0, 0.1, 0), 3.5, GOLD));
                later(level, 10, () -> pillar(level, player.position(), GOLD));
            }
            case ANCIENT_LIGHTNING_SPEAR -> later(level, 7, () -> line(level, player, f, 16.0, RED_LIGHTNING, 1.0));
            default -> {
            }
        }
    }

    private static void later(ServerLevel level, int ticks, Runnable r) {
        AshOfWarComboManager.scheduleDelayedAction(level, ticks, r);
    }

    private static Vec3 flatForward(Player p) {
        Vec3 l = p.getLookAngle();
        Vec3 flat = new Vec3(l.x, 0, l.z);
        return flat.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : flat.normalize();
    }

    /** Glowing crescent around the player: spanDeg wide, tilted by `tilt` radians (for combo variety). */
    private static void arc(ServerLevel level, Player player, Vec3 f, double radius, double spanDeg, Theme t, double tilt) {
        Vec3 right = new Vec3(-f.z, 0, f.x);
        Vec3 origin = player.position().add(0, player.getBbHeight() * 0.6, 0);
        int steps = (int) Math.max(16, spanDeg / 6);
        DustParticleOptions halo = new DustParticleOptions(t.halo, 3.2f);
        DustParticleOptions inner = new DustParticleOptions(t.core, 1.4f);
        for (int i = 0; i <= steps; i++) {
            double a = Math.toRadians(-spanDeg / 2 + spanDeg * i / steps);
            Vec3 dir = f.scale(Math.cos(a)).add(right.scale(Math.sin(a)));
            double y = Math.sin(a) * Math.sin(tilt) * radius * 0.5;
            for (double r = radius * 0.65; r <= radius; r += radius * 0.175) {
                Vec3 p = origin.add(dir.scale(r)).add(0, y, 0);
                level.sendParticles(halo, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
            Vec3 edge = origin.add(dir.scale(radius * 0.9)).add(0, y, 0);
            level.sendParticles(inner, edge.x, edge.y, edge.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(t.glow, edge.x, edge.y, edge.z, 1, 0.02, 0.02, 0.02, 0.0);
            if (i % 3 == 0) level.sendParticles(t.accent, edge.x, edge.y, edge.z, 1, 0.1, 0.1, 0.1, 0.01);
        }
        flash(level, origin.add(f.scale(radius * 0.8)), t, 1);
    }

    /** Glowing wave/beam travelling straight ahead. */
    private static void line(ServerLevel level, Player player, Vec3 f, double length, Theme t, double height) {
        Vec3 right = new Vec3(-f.z, 0, f.x);
        Vec3 origin = player.position().add(0, player.getBbHeight() * 0.5, 0);
        DustParticleOptions halo = new DustParticleOptions(t.halo, 3.6f);
        DustParticleOptions inner = new DustParticleOptions(t.core, 1.6f);
        for (double d = 1.0; d <= length; d += 0.45) {
            Vec3 c = origin.add(f.scale(d));
            for (double w = -height; w <= height + 1.0E-6; w += height / 2) {
                Vec3 p = c.add(right.scale(w * 0.6)).add(0, w * 0.5, 0);
                level.sendParticles(halo, p.x, p.y, p.z, 1, 0.04, 0.04, 0.04, 0.0);
            }
            level.sendParticles(inner, c.x, c.y, c.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(t.glow, c.x, c.y, c.z, 2, 0.05, 0.2, 0.05, 0.0);
            if (((int) (d * 2)) % 3 == 0) level.sendParticles(t.accent, c.x, c.y, c.z, 1, 0.15, 0.15, 0.15, 0.01);
        }
        flash(level, origin.add(f.scale(length)), t, 2);
    }

    /** Expanding glowing ring at a point. */
    private static void ring(ServerLevel level, Vec3 c, double radius, Theme t) {
        DustParticleOptions halo = new DustParticleOptions(t.halo, 3.0f);
        DustParticleOptions inner = new DustParticleOptions(t.core, 1.3f);
        int n = (int) (radius * 14);
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            double x = c.x + Math.cos(a) * radius, z = c.z + Math.sin(a) * radius;
            level.sendParticles(halo, x, c.y, z, 1, 0.05, 0.05, 0.05, 0.0);
            level.sendParticles(inner, c.x + Math.cos(a) * radius * 0.92, c.y, c.z + Math.sin(a) * radius * 0.92, 1, 0, 0, 0, 0);
            if (i % 2 == 0) level.sendParticles(t.glow, x, c.y, z, 1, 0.0, 0.05, 0.0, 0.0);
            if (i % 4 == 0) level.sendParticles(t.accent, x, c.y + 0.2, z, 1, 0.1, 0.1, 0.1, 0.02);
        }
        flash(level, c, t, 1);
    }

    /** Column of light rising from the ground (blessings, buffs). */
    private static void pillar(ServerLevel level, Vec3 base, Theme t) {
        DustParticleOptions halo = new DustParticleOptions(t.halo, 2.6f);
        for (double y = 0; y < 4.5; y += 0.3) {
            for (int i = 0; i < 6; i++) {
                double a = Math.PI * 2 * i / 6 + y;
                level.sendParticles(halo, base.x + Math.cos(a) * 0.9, base.y + y, base.z + Math.sin(a) * 0.9, 1, 0, 0, 0, 0);
            }
            level.sendParticles(t.glow, base.x, base.y + y, base.z, 2, 0.3, 0.05, 0.3, 0.01);
        }
        flash(level, base.add(0, 2.0, 0), t, 2);
    }

    /** The bloom pop: a white flash plus a burst of glow particles. */
    private static void flash(ServerLevel level, Vec3 p, Theme t, int strength) {
        level.sendParticles(ParticleTypes.FLASH, p.x, p.y, p.z, strength, 0.1, 0.1, 0.1, 0.0);
        level.sendParticles(t.glow, p.x, p.y, p.z, 10 * strength, 0.3, 0.3, 0.3, 0.08);
        level.sendParticles(new DustParticleOptions(t.halo, 4.0f), p.x, p.y, p.z, 6 * strength, 0.5, 0.5, 0.5, 0.0);
    }
}
