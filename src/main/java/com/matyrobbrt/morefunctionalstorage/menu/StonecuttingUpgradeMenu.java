package com.matyrobbrt.morefunctionalstorage.menu;

import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;

public class StonecuttingUpgradeMenu extends BaseUpgradeMenu {
    public StonecuttingUpgradeMenu(int containerId, Inventory playerInventory, BlockPos pos, int index) {
        super(MoreFunctionalStorage.STONECUTTING_MENU.get(), containerId, playerInventory, pos, index);
    }
}
