package com.matyrobbrt.morefunctionalstorage.client.screen;

import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IAsset;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.util.AssetUtil;
import com.matyrobbrt.morefunctionalstorage.menu.BreakerUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import java.util.List;

public class BreakerUpgradeScreen extends BaseUpgradeScreen<BreakerUpgradeMenu> {

    public BreakerUpgradeScreen(BreakerUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        getAddons().add(new BasicScreenAddon(80, 80) {
            @Override
            public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
                IAsset slot = IAssetProvider.getAsset(provider, AssetTypes.SLOT);
                AssetUtil.drawAsset(guiGraphics, screen, slot, guiX + this.getPosX() - 1, guiY + this.getPosY() - 1);
                var stack = menu.toolHandler.getStackInSlot(0);
                if (stack.isEmpty()) {
                    RenderingUtil.renderItem(guiGraphics, Items.IRON_PICKAXE.getDefaultInstance(), guiX + getPosX(), guiY + getPosY(),
                            0, 0, 1, 1, 1, 0.15f);
                } else {
                    guiGraphics.renderItem(stack, guiX + this.getPosX(), guiY + this.getPosY());
                }
            }

            @Override
            public List<Component> getTooltipLines() {
                return menu.toolHandler.getStackInSlot(0).isEmpty() ? List.of(Texts.NO_TOOL_INSTALLED) : List.of();
            }

            @Override
            public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int i, int i1, int i2, int i3, float v) {

            }

            @Override
            public int getXSize() {
                return 18;
            }

            @Override
            public int getYSize() {
                return 18;
            }
        });
    }
}
