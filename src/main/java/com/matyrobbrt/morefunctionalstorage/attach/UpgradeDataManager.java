package com.matyrobbrt.morefunctionalstorage.attach;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.matyrobbrt.morefunctionalstorage.item.MFSUpgrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpgradeDataManager implements INBTSerializable<CompoundTag> {
    private final Item owner;
    private CompoundTag tag;

    public UpgradeDataManager(Item owner) {
        this.owner = owner;
        this.tag = new CompoundTag();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return tag.copy();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        tag = nbt.copy();
    }

    public ValueHandler<Boolean> bool(String key) {
        return new ValueHandler<>() {
            @Override
            public Boolean get() {
                return tag.getBoolean(key);
            }

            @Override
            public void set(Boolean value) {
                tag.putBoolean(key, value);
            }
        };
    }

    public interface ValueHandler<T> {
        T get();
        void set(T value);
    }

    public static class Drawer implements INBTSerializable<CompoundTag> {
        private final ControllableDrawerTile<?> drawer;
        private final List<UpgradeDataManager> managers;
        public Drawer(ControllableDrawerTile<?> tile) {
            this.managers = new ArrayList<>(tile.getUtilitySlotAmount());
            for (int i = 0; i < tile.getUtilitySlotAmount(); i++) {
                managers.add(null);
            }
            this.drawer = tile;
        }

        public UpgradeDataManager getManager(ItemStack upgradeStack) {
            for (int i = 0; i < drawer.getUtilityUpgrades().getSlots(); i++) {
                if (drawer.getUtilityUpgrades().getStackInSlot(i) == upgradeStack) {
                    return getManager(i);
                }
            }
            return new UpgradeDataManager(upgradeStack.getItem());
        }

        public UpgradeDataManager getManager(int slot) {
            var man = managers.get(slot);
            var upgrade = drawer.getUtilityUpgrades().getStackInSlot(slot);
            if (man == null || man.owner != upgrade.getItem()) {
                man = new UpgradeDataManager(upgrade.getItem());
                managers.set(slot, man);
            }
            return man;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            var tag = new CompoundTag();
            for (int i = 0; i < managers.size(); i++) {
                var man = managers.get(i);
                if (man != null) {
                    tag.put("manager" + i, man.serializeNBT(provider));
                }
            }
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            Collections.fill(managers, null);
            for (int i = 0; i < managers.size(); i++) {
                if (nbt.contains("manager" + i) && drawer.getUtilityUpgrades().getStackInSlot(i).getItem() instanceof MFSUpgrade) {
                    var data = new UpgradeDataManager(drawer.getUtilityUpgrades().getStackInSlot(i).getItem());
                    data.deserializeNBT(provider, nbt.getCompound("manager" + i));
                    managers.set(i, data);
                }
            }
        }
    }
}
