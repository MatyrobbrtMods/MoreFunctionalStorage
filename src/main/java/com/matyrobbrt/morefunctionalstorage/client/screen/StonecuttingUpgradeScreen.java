package com.matyrobbrt.morefunctionalstorage.client.screen;

import com.matyrobbrt.morefunctionalstorage.menu.StonecuttingUpgradeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StonecuttingUpgradeScreen extends BaseUpgradeScreen<StonecuttingUpgradeMenu> {
    public StonecuttingUpgradeScreen(StonecuttingUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
