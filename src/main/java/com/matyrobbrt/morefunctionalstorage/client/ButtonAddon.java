package com.matyrobbrt.morefunctionalstorage.client;

import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.util.AssetUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public abstract class ButtonAddon extends BasicScreenAddon {
    protected final int width, height;

    protected ButtonAddon(int posX, int posY, int width, int height) {
        super(posX, posY);
        this.width = width;
        this.height = height;
    }

    @Override
    public int getXSize() {
        return width;
    }

    @Override
    public int getYSize() {
        return height;
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        if (this.isMouseOver(mouseX - guiX, mouseY - guiY)) {
            AssetUtil.drawSelectingOverlay(guiGraphics, this.getPosX() + 1, this.getPosY() + 1, this.getPosX() + this.getXSize() - 1, this.getPosY() + this.getYSize() - 1);
        }
    }

    public abstract boolean click(double mouseX, double mouseY, int button);

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var screen = (AbstractContainerScreen)Minecraft.getInstance().screen;
        if (this.isMouseOver(mouseX - screen.getGuiLeft(), mouseY - screen.getGuiTop())) {
            if (click(mouseX, mouseY, button)) {
                Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.0F, RandomSource.create(), Minecraft.getInstance().player.blockPosition()));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
