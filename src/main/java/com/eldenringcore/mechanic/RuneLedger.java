package com.eldenringcore.mechanic;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class RuneLedger extends SavedData {
    private static final String FILE_NAME = "eldenring_core_runes";
    private static final Account EMPTY = new Account(0, null);
    private final Map<UUID, Account> accounts = new HashMap<>();

    static RuneLedger get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(RuneLedger::load, RuneLedger::new, FILE_NAME);
    }

    Account getAccount(UUID player) {
        return accounts.getOrDefault(player, EMPTY);
    }

    void put(UUID player, int balance, Grave grave) {
        if (balance == 0 && grave == null) {
            accounts.remove(player);
        } else {
            accounts.put(player, new Account(balance, grave));
        }
        setDirty();
    }

    private static RuneLedger load(CompoundTag tag) {
        RuneLedger ledger = new RuneLedger();
        ListTag entries = tag.getList("Accounts", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            if (!entry.hasUUID("Player")) {
                continue;
            }
            CompoundTag graveTag = entry.getCompound("Grave");
            ResourceLocation dimension = ResourceLocation.tryParse(graveTag.getString("Dimension"));
            int amount = graveTag.getInt("Amount");
            Grave grave = dimension != null && amount > 0 && graveTag.hasUUID("Marker")
                    ? new Grave(ResourceKey.create(Registries.DIMENSION, dimension), graveTag.getUUID("Marker"), amount)
                    : null;
            ledger.accounts.put(entry.getUUID("Player"), new Account(Math.max(0, entry.getInt("Balance")), grave));
        }
        return ledger;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        accounts.forEach((player, account) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", player);
            entry.putInt("Balance", account.balance());
            Grave grave = account.grave();
            if (grave != null) {
                CompoundTag graveTag = new CompoundTag();
                graveTag.putString("Dimension", grave.dimension().location().toString());
                graveTag.putUUID("Marker", grave.markerId());
                graveTag.putInt("Amount", grave.amount());
                entry.put("Grave", graveTag);
            }
            entries.add(entry);
        });
        tag.put("Accounts", entries);
        return tag;
    }

    record Account(int balance, Grave grave) {}

    record Grave(ResourceKey<Level> dimension, UUID markerId, int amount) {}
}
