package com.matyrobbrt.morefunctionalstorage.menu;

import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;

public class RefillUpgradeMenu extends BaseUpgradeMenu {
    public RefillUpgradeMenu(int containerId, Inventory playerInventory, BlockPos pos, int index) {
        super(MoreFunctionalStorage.REFILL_MENU.get(), containerId, playerInventory, pos, index);
    }
}
