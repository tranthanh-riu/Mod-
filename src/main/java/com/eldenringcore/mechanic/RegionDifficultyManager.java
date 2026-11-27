package com.eldenringcore.mechanic;

import com.eldenringcore.boss.EldenBossEntity;
import com.eldenringcore.combat.StatusBuildupManager;
import com.eldenringcore.combat.StatusEffectType;
import com.eldenringcore.registry.ModWorldgen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentic Elden Ring Area Difficulty Scaling & Region Entry Banners:
 * - Displays iconic Region Title + Danger Tier banner when entering each of the 7 regions.
 * - Scales non-boss hostile mob HP, Damage, and Armor according to Elden Ring area scaling (Limgrave -> Haligtree).
 * - Adds regional environmental hazards (e.g., standing in water in Caelid builds up Scarlet Rot!).
 */
public class RegionDifficultyManager {
    private static final String TAG_SCALED = "eldenring_region_scaled";
    private static final Map<UUID, String> LAST_REGION = new ConcurrentHashMap<>();

    public record RegionProfile(
            String displayName,
            String subtitle,
            ChatFormatting color,
            double hpMult,
            double dmgMult,
            double bonusArmor,
            boolean fireImmuneMobs,
            boolean rotWaterHazard
    ) {}

    private static final Map<String, RegionProfile> PROFILES = Map.of(
            "eldenring_core:limgrave", new RegionProfile(
                    "LIMGRAVE", "Tier I — Vùng Khởi Đầu (Máu x1.0 | Sát thương x1.0)",
                    ChatFormatting.GOLD, 1.0, 1.0, 0.0, false, false),
            "eldenring_core:liurnia", new RegionProfile(
                    "LIURNIA OF THE LAKES", "Tier II — Vùng Đầm Lầy Phép Thuật (Máu x1.5 | ST x1.35)",
                    ChatFormatting.AQUA, 1.5, 1.35, 2.0, false, false),
            "eldenring_core:caelid", new RegionProfile(
                    "CAELID", "Tier III — Vùng Đất Mục Rữa Scarlet Rot (Máu x2.2 | ST x1.75)",
                    ChatFormatting.DARK_RED, 2.2, 1.75, 4.0, false, true),
            "eldenring_core:altus_plateau", new RegionProfile(
                    "ALTUS PLATEAU", "Tier IV — Cao Nguyên Hoàng Kim (Máu x2.6 | ST x2.0)",
                    ChatFormatting.YELLOW, 2.6, 2.0, 6.0, false, false),
            "eldenring_core:mt_gelmir", new RegionProfile(
                    "MT. GELMIR", "Tier IV — Núi Lửa Dung Nham (Máu x2.8 | ST x2.1 | Kháng Lửa)",
                    ChatFormatting.RED, 2.8, 2.1, 6.0, true, false),
            "eldenring_core:mountaintops", new RegionProfile(
                    "MOUNTAINTOPS OF THE GIANTS", "Tier V — Đỉnh Núi Người Khổng Lồ (Máu x3.5 | ST x2.5)",
                    ChatFormatting.WHITE, 3.5, 2.5, 10.0, false, false),
            "eldenring_core:haligtree", new RegionProfile(
                    "MIQUELLA'S HALIGTREE", "Tier VI — Vùng Endgame Khó Nhất (Máu x4.5 | ST x3.0)",
                    ChatFormatting.LIGHT_PURPLE, 4.5, 3.0, 12.0, false, false)
    );

    @SubscribeEvent
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().equals(ModWorldgen.LANDS_BETWEEN_LEVEL)) return;
        if (!(event.getEntity() instanceof Monster mob) || mob instanceof EldenBossEntity) return;
        if (mob.getPersistentData().getBoolean(TAG_SCALED)) return;

        String biomeId = getBiomeId(event.getLevel().getBiome(mob.blockPosition()));
        RegionProfile profile = PROFILES.get(biomeId);
        if (profile == null) return;

        mob.getPersistentData().putBoolean(TAG_SCALED, true);

        AttributeInstance maxHp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHp != null && profile.hpMult() > 1.0) {
            maxHp.setBaseValue(maxHp.getBaseValue() * profile.hpMult());
            mob.setHealth(mob.getMaxHealth());
        }
        AttributeInstance atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null && profile.dmgMult() > 1.0) {
            atk.setBaseValue(atk.getBaseValue() * profile.dmgMult());
        }
        AttributeInstance armor = mob.getAttribute(Attributes.ARMOR);
        if (armor != null && profile.bonusArmor() > 0.0) {
            armor.setBaseValue(armor.getBaseValue() + profile.bonusArmor());
        }
        if (profile.fireImmuneMobs()) {
            mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        if (!player.level().dimension().equals(ModWorldgen.LANDS_BETWEEN_LEVEL)) {
            LAST_REGION.remove(player.getUUID());
            return;
        }

        String biomeId = getBiomeId(player.level().getBiome(player.blockPosition()));
        RegionProfile profile = PROFILES.get(biomeId);
        if (profile == null) return;

        String prev = LAST_REGION.put(player.getUUID(), biomeId);
        if (!biomeId.equals(prev)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 55, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.literal(profile.displayName()).withStyle(profile.color(), ChatFormatting.BOLD)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.literal(profile.subtitle()).withStyle(ChatFormatting.GRAY)));
            player.playSound(SoundEvents.AMBIENT_CAVE.get(), 0.7f, 0.85f);
        }

        // Authentic Caelid Hazard: wading in Swamp of Aeonia water builds up Scarlet Rot!
        if (profile.rotWaterHazard() && player.isInWater() && !player.isCreative() && !player.isSpectator()) {
            StatusBuildupManager.addBuildup(player, null, StatusEffectType.SCARLET_ROT, 18.0f);
        }
    }

    private static String getBiomeId(Holder<Biome> holder) {
        return holder.unwrapKey()
                .map(ResourceKey::location)
                .map(ResourceLocation::toString)
                .orElse("");
    }
}
