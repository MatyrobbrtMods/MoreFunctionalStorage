package com.matyrobbrt.morefunctionalstorage.menu;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;

public class BreakerUpgradeMenu extends BaseUpgradeMenu {
    public ComponentItemHandler toolHandler;
    public BreakerUpgradeMenu(int containerId, Inventory playerInventory, BlockPos pos, int index) {
        super(MoreFunctionalStorage.BREAKER_MENU.get(), containerId, playerInventory, pos, index);
    }

    @Override
    protected void addSlots(ControllableDrawerTile<?> drawer, int upgradeSlot) {
        super.addSlots(drawer, upgradeSlot);

        toolHandler = new ComponentItemHandler(drawer.getUtilityUpgrades().getStackInSlot(upgradeSlot), MoreFunctionalStorage.TOOL.get(), 1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.isEmpty() || stack.has(DataComponents.TOOL);
            }
        };
        addSlot(new ItemHandlerCopySlot(toolHandler, 0, 80, 80) {
            @Override
            protected void setStackCopy(ItemStack stack) {
                toolHandler.setStackInSlot(0, stack);
            }
        });
    }
}
