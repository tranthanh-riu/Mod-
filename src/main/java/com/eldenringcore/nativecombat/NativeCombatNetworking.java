package com.eldenringcore.nativecombat;

import com.eldenringcore.EldenRingCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class NativeCombatNetworking {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(EldenRingCore.MOD_ID, "native_combat"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int nextId = 0;

    public static void register() {
        CHANNEL.messageBuilder(C2SCombatActionPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SCombatActionPacket::encode)
                .decoder(C2SCombatActionPacket::decode)
                .consumerMainThread(C2SCombatActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CPlayAnimPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CPlayAnimPacket::encode)
                .decoder(S2CPlayAnimPacket::decode)
                .consumerMainThread(S2CPlayAnimPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSyncStaminaPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncStaminaPacket::encode)
                .decoder(S2CSyncStaminaPacket::decode)
                .consumerMainThread(S2CSyncStaminaPacket::handle)
                .add();
    }

    public static void sendAction(NativeKatanaItem.C2SAction action, float moveX, float moveZ) {
        CHANNEL.sendToServer(new C2SCombatActionPacket(action.ordinal(), moveX, moveZ));
    }

    public static void broadcastAnimation(ServerPlayer source, String animId, float speed) {
        S2CPlayAnimPacket pkt = new S2CPlayAnimPacket(source.getId(), animId, speed);
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> source), pkt);
    }

    public static void syncStamina(ServerPlayer player, float stamina, float maxStamina, int comboStep) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CSyncStaminaPacket(stamina, maxStamina, comboStep));
    }

    public record C2SCombatActionPacket(int actionOrdinal, float moveX, float moveZ) {
        public static void encode(C2SCombatActionPacket msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.actionOrdinal);
            buf.writeFloat(msg.moveX);
            buf.writeFloat(msg.moveZ);
        }

        public static C2SCombatActionPacket decode(FriendlyByteBuf buf) {
            return new C2SCombatActionPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat());
        }

        public static void handle(C2SCombatActionPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
            NetworkEvent.Context ctx = ctxSup.get();
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            NativeKatanaItem.C2SAction[] vals = NativeKatanaItem.C2SAction.values();
            if (msg.actionOrdinal < 0 || msg.actionOrdinal >= vals.length) return;
            NativeCombatManager.handleAction(player, vals[msg.actionOrdinal], msg.moveX, msg.moveZ);
        }
    }

    public record S2CPlayAnimPacket(int entityId, String animId, float speed) {
        public static void encode(S2CPlayAnimPacket msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.entityId);
            buf.writeUtf(msg.animId);
            buf.writeFloat(msg.speed);
        }

        public static S2CPlayAnimPacket decode(FriendlyByteBuf buf) {
            return new S2CPlayAnimPacket(buf.readVarInt(), buf.readUtf(), buf.readFloat());
        }

        public static void handle(S2CPlayAnimPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.eldenringcore.client.NativeCombatClient.playAnimationOnEntity(msg.entityId, msg.animId, msg.speed));
        }
    }

    public record S2CSyncStaminaPacket(float stamina, float maxStamina, int comboStep) {
        public static void encode(S2CSyncStaminaPacket msg, FriendlyByteBuf buf) {
            buf.writeFloat(msg.stamina);
            buf.writeFloat(msg.maxStamina);
            buf.writeVarInt(msg.comboStep);
        }

        public static S2CSyncStaminaPacket decode(FriendlyByteBuf buf) {
            return new S2CSyncStaminaPacket(buf.readFloat(), buf.readFloat(), buf.readVarInt());
        }

        public static void handle(S2CSyncStaminaPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.eldenringcore.client.NativeCombatClient.updateStaminaHud(msg.stamina, msg.maxStamina, msg.comboStep));
        }
    }
}
