package com.matyrobbrt.morefunctionalstorage.client.screen;

import com.matyrobbrt.morefunctionalstorage.menu.PlacerUpgradeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PlacerUpgradeScreen extends BaseUpgradeScreen<PlacerUpgradeMenu> {
    public PlacerUpgradeScreen(PlacerUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
