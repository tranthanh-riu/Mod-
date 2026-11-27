package com.eldenringcore.combat;

import com.eldenringcore.EldenRingCore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages multi-stage Ash of War combos (e.g. Corpse Piler 3-hit chain)
 * and lightweight server-tick delayed slash callbacks.
 */
@Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AshOfWarComboManager {
    private static final Map<UUID, ComboState> COMBO_STATES = new ConcurrentHashMap<>();
    private static final List<ScheduledAction> SCHEDULED_ACTIONS = new CopyOnWriteArrayList<>();

    private record ComboState(AshOfWarType type, int stage, long expiryTick) {
    }

    private static final class ScheduledAction {
        final ServerLevel level;
        final long executeTick;
        final Runnable action;

        ScheduledAction(ServerLevel level, long executeTick, Runnable action) {
            this.level = level;
            this.executeTick = executeTick;
            this.action = action;
        }
    }

    public static int getActiveComboStage(Player player, AshOfWarType type) {
        ComboState state = COMBO_STATES.get(player.getUUID());
        if (state == null || state.type() != type) {
            return 0;
        }
        if (player.level().getGameTime() > state.expiryTick()) {
            COMBO_STATES.remove(player.getUUID());
            return 0;
        }
        return state.stage();
    }

    public static void setComboStage(Player player, AshOfWarType type, int nextStage, int windowTicks) {
        long expiry = player.level().getGameTime() + windowTicks;
        COMBO_STATES.put(player.getUUID(), new ComboState(type, nextStage, expiry));
    }

    public static void resetCombo(Player player) {
        COMBO_STATES.remove(player.getUUID());
    }

    public static void scheduleDelayedAction(ServerLevel level, int delayTicks, Runnable action) {
        long targetTick = level.getGameTime() + Math.max(1, delayTicks);
        SCHEDULED_ACTIONS.add(new ScheduledAction(level, targetTick, action));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || SCHEDULED_ACTIONS.isEmpty()) {
            return;
        }
        Iterator<ScheduledAction> it = SCHEDULED_ACTIONS.iterator();
        while (it.hasNext()) {
            ScheduledAction scheduled = it.next();
            if (scheduled.level.getGameTime() >= scheduled.executeTick) {
                SCHEDULED_ACTIONS.remove(scheduled);
                scheduled.action.run();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        COMBO_STATES.remove(event.getEntity().getUUID());
    }
}
