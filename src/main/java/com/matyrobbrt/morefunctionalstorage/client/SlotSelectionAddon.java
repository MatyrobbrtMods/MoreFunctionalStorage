package com.matyrobbrt.morefunctionalstorage.client;

import com.buuz135.functionalstorage.client.gui.DrawerInfoGuiAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.client.screen.BaseUpgradeScreen;
import com.matyrobbrt.morefunctionalstorage.mixin.DrawerInfoGuiAddonAccess;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SlotSelectionAddon extends DrawerInfoGuiAddon {
    public static final int SELECTED = 0xFFFF006E;
    public static final int HOVERED = 0xFF0026FF;

    private final BaseUpgradeScreen<?> screen;
    private final boolean source;
    public SlotSelectionAddon(int x, int y, DrawerInfoGuiAddonAccess acc, BaseUpgradeScreen<?> screen, boolean source) {
        super(x, y, acc.mfs$getGui(), acc.mfs$getSlotAmount(), acc.mfs$getSlotPosition(), acc.mfs$getSlotStack(), acc.mfs$getSlotMaxAmount());
        this.screen = screen;
        this.source = source;
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        var ad = ((DrawerInfoGuiAddonAccess) this);
        var selected = this.screen.getMenu().getUpgrade().get(MoreFunctionalStorage.SELECTED_SLOTS);

        for (var i = 0; i < ad.mfs$getSlotAmount(); i++) {
            var x = ad.mfs$getSlotPosition().apply(i).getLeft() + getPosX();
            var y = ad.mfs$getSlotPosition().apply(i).getRight() + getPosY();

            boolean isHovered = (mouseX - guiX) > x && (mouseX - guiX) < x + 18 && (mouseY - guiY) > y && (mouseY - guiY) < y + 18;

            if (isHovered && selected != null && !selected.contains(i)) {
                colorAround(guiGraphics, x, y, HOVERED);
                guiGraphics.renderTooltip(Minecraft.getInstance().font, List.of(
                        Texts.LCLICK_SELECT.format(source ? Texts.SOURCE : Texts.DESTINATION)
                ), Optional.empty(), mouseX - guiX, mouseY - guiY);
            } else if (selected == null || selected.contains(i)) {
                colorAround(guiGraphics, x, y, SELECTED);
                if (isHovered) {
                    guiGraphics.renderTooltip(Minecraft.getInstance().font, List.of(
                            Texts.SLOT_SELECTED.format(source ? Texts.SOURCE : Texts.DESTINATION), Texts.LCLICK_DESELECT
                    ), Optional.empty(), mouseX - guiX, mouseY - guiY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(mouseX, mouseY, button);

        mouseX -= screen.getGuiLeft();
        mouseY -= screen.getGuiTop();

        var ad = ((DrawerInfoGuiAddonAccess) this);
        var selected = this.screen.getMenu().getUpgrade().get(MoreFunctionalStorage.SELECTED_SLOTS);

        for (var i = 0; i < ad.mfs$getSlotAmount(); i++) {
            int currentIndex = i;
            var x = ad.mfs$getSlotPosition().apply(i).getLeft() + getPosX();
            var y = ad.mfs$getSlotPosition().apply(i).getRight() + getPosY();

            if (mouseX > x && mouseX < x + 18 && mouseY > y && mouseY < y + 18) {
                if (selected == null) {
                    selected = IntStream.rangeClosed(0, ad.mfs$getSlotAmount())
                            .filter(j -> j != currentIndex)
                            .mapToObj(Integer::valueOf)
                            .sorted()
                            .toList();
                } else if (selected.contains(i)) {
                    selected = selected.stream()
                            .filter(j -> j != currentIndex && j < ad.mfs$getSlotAmount())
                            .sorted().toList();
                } else {
                    selected = Stream.concat(selected.stream()
                            .filter(j -> j < ad.mfs$getSlotAmount()), Stream.of(currentIndex))
                            .sorted().toList();

                    if (selected.size() == ad.mfs$getSlotAmount()) {
                        selected = null;
                    }
                }
                this.screen.getMenu().getUpgrade().set(MoreFunctionalStorage.SELECTED_SLOTS, selected);
                this.screen.getMenu().notifyRebuild();

                return true;
            }
        }

        return false;
    }

    private void colorAround(GuiGraphics guiGraphics, int x, int y, int colour) {
        guiGraphics.pose().translate(0, 0, 200);

        // up line
        guiGraphics.fill(x - 1, y - 1, x + 17, y, colour);
        // down line
        guiGraphics.fill(x - 1, y + 16, x + 17, y + 17, colour);

        // left line
        guiGraphics.fill(x - 1, y - 1, x, y + 17, colour);
        // right line
        guiGraphics.fill(x + 16, y - 1, x + 17, y + 17, colour);

        guiGraphics.pose().translate(0, 0, -200);
    }
}
