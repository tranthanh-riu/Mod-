package com.eldenringcore.mechanic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.List;

final class GraceTravelMenu extends ChestMenu {
    private static final int PAGE_SIZE = 45;
    private static final int PREVIOUS_SLOT = 45;
    private static final int NEXT_SLOT = 53;

    private final GlobalPos origin;
    private final List<GlobalPos> destinations;
    private int page;

    static void open(ServerPlayer player, BlockPos position) {
        GlobalPos origin = GlobalPos.of(player.level().dimension(), position.immutable());
        List<GlobalPos> destinations = GraceLocations.destinations(player, origin);
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new GraceTravelMenu(id, inventory, origin, destinations),
                Component.translatable("menu.eldenring_core.grace.title")));
    }

    private GraceTravelMenu(int id, Inventory inventory, GlobalPos origin, List<GlobalPos> destinations) {
        super(MenuType.GENERIC_9x6, id, inventory, new SimpleContainer(54), 6);
        this.origin = origin;
        this.destinations = destinations;
        showPage((ServerPlayer) inventory.player);
    }

    private void showPage(ServerPlayer player) {
        getContainer().clearContent();
        int first = page * PAGE_SIZE;
        int end = Math.min(destinations.size(), first + PAGE_SIZE);
        for (int index = first; index < end; index++) {
            GlobalPos destination = destinations.get(index);
            ServerLevel level = player.serverLevel().getServer().getLevel(destination.dimension());
            if (level != null) {
                ItemStack entry = new ItemStack(Items.SOUL_LANTERN);
                entry.setHoverName(GraceLocations.title(level, destination.pos()));
                getContainer().setItem(index - first, entry);
            }
        }
        if (destinations.isEmpty()) {
            ItemStack empty = new ItemStack(Items.PAPER);
            empty.setHoverName(Component.translatable("menu.eldenring_core.grace.empty"));
            getContainer().setItem(22, empty);
        }
        if (page > 0) {
            setPageButton(PREVIOUS_SLOT, "menu.eldenring_core.grace.previous");
        }
        if (end < destinations.size()) {
            setPageButton(NEXT_SLOT, "menu.eldenring_core.grace.next");
        }
        broadcastChanges();
    }

    private void setPageButton(int slot, String translation) {
        ItemStack arrow = new ItemStack(Items.ARROW);
        arrow.setHoverName(Component.translatable(translation));
        getContainer().setItem(slot, arrow);
    }

    @Override
    public boolean stillValid(Player player) {
        return player instanceof ServerPlayer serverPlayer && player.isAlive() && !player.isSpectator()
                && player.level().dimension().equals(origin.dimension())
                && player.distanceToSqr(origin.pos().getX() + 0.5, origin.pos().getY() + 0.5, origin.pos().getZ() + 0.5) <= 64
                && GraceLocations.isGrace(serverPlayer.serverLevel(), origin.pos());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slot, int button, ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || clickType != ClickType.PICKUP || !stillValid(player)) {
            return;
        }
        int index = page * PAGE_SIZE + slot;
        if (slot == PREVIOUS_SLOT && page > 0) {
            page--;
            showPage(serverPlayer);
        } else if (slot == NEXT_SLOT && (page + 1) * PAGE_SIZE < destinations.size()) {
            page++;
            showPage(serverPlayer);
        } else if (slot >= 0 && slot < PAGE_SIZE && index < destinations.size()) {
            travel(serverPlayer, destinations.get(index));
        }
    }

    private static void travel(ServerPlayer player, GlobalPos destination) {
        ServerLevel level = player.serverLevel().getServer().getLevel(destination.dimension());
        BlockPos grace = destination.pos();
        BlockPos landing = grace.above();
        double x = landing.getX() + 0.5;
        double y = landing.getY();
        double z = landing.getZ() + 0.5;
        AABB arrival = player.getDimensions(Pose.STANDING).makeBoundingBox(x, y, z);
        if (level != null && level.isInWorldBounds(landing.above()) && level.getWorldBorder().isWithinBounds(arrival)
                && GraceLocations.isGrace(level, grace) && level.noCollision(player, arrival)
                && !level.containsAnyLiquid(arrival)) {
            player.closeContainer();
            player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
        } else {
            player.sendSystemMessage(Component.translatable("message.eldenring_core.grace.destination_unavailable"));
        }
    }
}
