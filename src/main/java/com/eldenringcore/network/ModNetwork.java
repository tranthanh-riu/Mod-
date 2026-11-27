package com.eldenringcore.network;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.combat.EldenWeaponItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

/** Network channel for eldenring_core. */
public final class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(EldenRingCore.MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, AshOfWarPacket.class, AshOfWarPacket::encode, buf -> new AshOfWarPacket(),
                AshOfWarPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    /** Client pressed Epic Fight's "Weapon Innate Skill" key while holding an Elden weapon. */
    public record AshOfWarPacket() {
        static void handle(AshOfWarPacket msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context c = ctx.get();
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                if (player == null || player.isSpectator()) return;
                ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (stack.getItem() instanceof EldenWeaponItem weapon) {
                    weapon.activateAshOfWar(player.serverLevel(), player, stack);
                }
            });
            c.setPacketHandled(true);
        }

        @SuppressWarnings("unused")
        static void encode(AshOfWarPacket msg, FriendlyByteBuf buf) {
        }
    }
}
