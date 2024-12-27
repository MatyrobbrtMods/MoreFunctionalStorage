package com.matyrobbrt.morefunctionalstorage.client.screen;

import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.client.ButtonAddon;
import com.matyrobbrt.morefunctionalstorage.item.RefillUpgrade.RefillTarget;
import com.matyrobbrt.morefunctionalstorage.menu.RefillUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class RefillUpgradeScreen extends BaseUpgradeScreen<RefillUpgradeMenu> {
    private static final ResourceLocation LOCATION_TEXTURE = ResourceLocation.fromNamespaceAndPath(MoreFunctionalStorage.MOD_ID, "textures/gui/refill_location.png");
    public RefillUpgradeScreen(RefillUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        getAddons().add(new ButtonAddon(153 - 2 * 18, 28, 14, 14) {
            private RefillTarget getTarget() {
                return menu.getUpgrade().getOrDefault(MoreFunctionalStorage.REFILL_TARGET, RefillTarget.HOTBAR);
            }

            @Override
            public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
                var current = getTarget();
                guiGraphics.blit(LOCATION_TEXTURE, this.getPosX() + guiX, this.getPosY() + guiY, current.ordinal() * 14, 0, 14, 14, 42, 14);
            }

            @Override
            public boolean click(double mouseX, double mouseY, int button) {
                int dir = button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? 1 : -1;
                int next = getTarget().ordinal() + dir;
                if (next >= RefillTarget.values().length) next = 0;
                else if (next < 0) next = RefillTarget.values().length - 1;

                menu.getUpgrade().set(MoreFunctionalStorage.REFILL_TARGET, RefillTarget.values()[next]);
                menu.notifyRebuild();

                return true;
            }

            @Override
            public List<Component> getTooltipLines() {
                var target = getTarget();
                return List.of(Texts.REFILL_TARGET.format(
                        target.displayText.copy().withStyle(ChatFormatting.AQUA)));
            }
        });
    }
}
