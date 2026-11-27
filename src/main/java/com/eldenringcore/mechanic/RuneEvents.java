package com.eldenringcore.mechanic;

import com.eldenringcore.EldenRingCore;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID)
public final class RuneEvents {
    private static final String OWNER_TAG = "eldenring_core_rune_owner";
    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0f, 0.75f, 0.17f), 1.25f);

    private RuneEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        RuneLedger ledger = RuneLedger.get(level.getServer());
        UUID owner = player.getUUID();
        RuneLedger.Account account = ledger.getAccount(owner);
        RuneLedger.Grave oldGrave = account.grave();
        if (oldGrave != null) {
            ServerLevel graveLevel = level.getServer().getLevel(oldGrave.dimension());
            if (graveLevel != null && graveLevel.getEntity(oldGrave.markerId()) instanceof Marker oldMarker) {
                oldMarker.discard();
            }
            ledger.put(owner, account.balance(), null);
        }
        if (account.balance() <= 0) {
            return;
        }

        Marker marker = EntityType.MARKER.create(level);
        if (marker == null) {
            return;
        }
        marker.setPos(player.getX(), player.getY(), player.getZ());
        marker.getPersistentData().putUUID(OWNER_TAG, owner);
        if (level.addFreshEntity(marker)) {
            ledger.put(owner, 0, new RuneLedger.Grave(level.dimension(), marker.getUUID(), account.balance()));
        }
    }

    @SubscribeEvent
    public static void removeExpiredMarker(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Marker marker)
                || !event.loadedFromDisk() || !marker.getPersistentData().hasUUID(OWNER_TAG)) {
            return;
        }
        RuneLedger.Grave grave = RuneLedger.get(level.getServer()).getAccount(marker.getPersistentData().getUUID(OWNER_TAG)).grave();
        if (grave == null || !grave.markerId().equals(marker.getUUID()) || !grave.dimension().equals(level.dimension())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator() || player.tickCount % 5 != 0) {
            return;
        }
        ServerLevel level = player.serverLevel();
        RuneLedger ledger = RuneLedger.get(level.getServer());
        RuneLedger.Account account = ledger.getAccount(player.getUUID());
        RuneLedger.Grave grave = account.grave();
        if (grave == null || !grave.dimension().equals(level.dimension())
                || !(level.getEntity(grave.markerId()) instanceof Marker marker)) {
            return;
        }
        double distance = player.distanceToSqr(marker);
        if (distance <= 1.44) {
            int recovered = Math.min(grave.amount(), Integer.MAX_VALUE - account.balance());
            if (recovered == 0) {
                return;
            }
            int remaining = grave.amount() - recovered;
            RuneLedger.Grave remainder = remaining == 0 ? null : new RuneLedger.Grave(grave.dimension(), grave.markerId(), remaining);
            ledger.put(player.getUUID(), account.balance() + recovered, remainder);
            if (remainder == null) {
                marker.discard();
            }
            player.displayClientMessage(Component.translatable("message.eldenring_core.runes.recovered", recovered)
                    .withStyle(ChatFormatting.GOLD), true);
        } else if (distance <= 4096.0) {
            for (int height = 0; height < 7; height++) {
                level.sendParticles(player, GOLD, true, marker.getX(), marker.getY() + height * 0.35, marker.getZ(),
                        1, 0.08, 0.08, 0.08, 0.0);
            }
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("runes")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    int balance = RuneLedger.get(context.getSource().getServer()).getAccount(player.getUUID()).balance();
                    context.getSource().sendSuccess(() -> Component.translatable("message.eldenring_core.runes.balance", balance), false);
                    return balance;
                })
                .then(Commands.literal("add").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    RuneLedger ledger = RuneLedger.get(context.getSource().getServer());
                                    RuneLedger.Account account = ledger.getAccount(player.getUUID());
                                    int balance = add(account.balance(), IntegerArgumentType.getInteger(context, "amount"));
                                    ledger.put(player.getUUID(), balance, account.grave());
                                    context.getSource().sendSuccess(() -> Component.translatable("message.eldenring_core.runes.granted",
                                            player.getDisplayName(), balance), true);
                                    return balance;
                                })))));
    }

    private static int add(int balance, int amount) {
        return (int) Math.min(Integer.MAX_VALUE, (long) balance + amount);
    }
}
