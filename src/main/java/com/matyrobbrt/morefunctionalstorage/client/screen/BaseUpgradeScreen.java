package com.matyrobbrt.morefunctionalstorage.client.screen;

import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IAsset;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.block.redstone.RedstoneAction;
import com.hrznstudio.titanium.block.redstone.RedstoneManager;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.addon.StateButtonAddon;
import com.hrznstudio.titanium.client.screen.addon.StateButtonInfo;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.client.screen.container.BasicContainerScreen;
import com.hrznstudio.titanium.component.button.RedstoneControlButtonComponent;
import com.hrznstudio.titanium.util.AssetUtil;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.client.ButtonAddon;
import com.matyrobbrt.morefunctionalstorage.client.FiltersPanelAddon;
import com.matyrobbrt.morefunctionalstorage.client.SlotSelectionAddon;
import com.matyrobbrt.morefunctionalstorage.item.MFSUpgrade;
import com.matyrobbrt.morefunctionalstorage.menu.BaseUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.mixin.DrawerInfoGuiAddonAccess;
import com.matyrobbrt.morefunctionalstorage.packet.OpenDrawerMenuPayload;
import com.matyrobbrt.morefunctionalstorage.util.RelativeDirection;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BaseUpgradeScreen<T extends BaseUpgradeMenu> extends BasicContainerScreen<T> {
    public static final ResourceLocation DIRECTIONS_TEXTURE = ResourceLocation.fromNamespaceAndPath(MoreFunctionalStorage.MOD_ID, "textures/gui/directions.png");
    public static final ResourceLocation FILTERS_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(MoreFunctionalStorage.MOD_ID, "textures/gui/filters_button.png");

    private final FiltersPanelAddon filterAddon;
    private boolean filtersShown;

    public BaseUpgradeScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        for (IFactory<? extends IScreenAddon> screenAddon : menu.drawer.getScreenAddons()) {
            if (screenAddon.create() instanceof DrawerInfoGuiAddonAccess ad) {
                getAddons().add(new SlotSelectionAddon(8 + 2 * 18, 28, ad, this, true));
            }
        }

        var type = (MFSUpgrade) menu.getUpgrade().getItem();

        this.filterAddon = new FiltersPanelAddon(176, 10, menu);

        getAddons().add(new BasicScreenAddon(152, 80) {
            @Override
            public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
                IAsset slot = IAssetProvider.getAsset(provider, AssetTypes.SLOT);
                AssetUtil.drawAsset(guiGraphics, screen, slot, guiX + this.getPosX() - 1, guiY + this.getPosY() - 1);
                guiGraphics.renderItem(menu.augmentHandler.getStackInSlot(0), guiX + this.getPosX(), guiY + this.getPosY());
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

        if (type.hasDirection()) {
            getAddons().add(new ButtonAddon(153 - 2 * 18, 28, 14, 14) {
                private RelativeDirection getDir() {
                    return menu.getUpgrade().getOrDefault(MoreFunctionalStorage.RELATIVE_DIRECTION, RelativeDirection.FRONT);
                }

                @Override
                public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
                    var current = getDir();
                    guiGraphics.blit(DIRECTIONS_TEXTURE, this.getPosX() + guiX, this.getPosY() + guiY, current.ordinal() * 14, 0, 14, 14, 84, 14);
                }

                @Override
                public boolean click(double mouseX, double mouseY, int button) {
                    int dir = button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? 1 : -1;
                    int next = getDir().ordinal() + dir;
                    if (next >= RelativeDirection.values().length) next = 0;
                    else if (next < 0) next = RelativeDirection.values().length - 1;

                    menu.getUpgrade().set(MoreFunctionalStorage.RELATIVE_DIRECTION, RelativeDirection.values()[next]);
                    menu.notifyRebuild();

                    return true;
                }

                @Override
                public List<Component> getTooltipLines() {
                    var dir = getDir();
                    var absolute = dir.getAbsolute(menu.drawer);
                    return List.of(Texts.DIRECTION.format(
                            dir.getDisplayText().withStyle(ChatFormatting.AQUA),
                            Component.translatable(WordUtils.capitalize(absolute.getSerializedName()))));
                }
            });
        }

        getAddons().add(new ButtonAddon(153 - 2 * 18, 28 + 14 + 3, 14, 14) {
            @Override
            public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
                guiGraphics.blit(FILTERS_BUTTON_TEXTURE, this.getPosX() + guiX, this.getPosY() + guiY, 0, 0, 14, 14, 14, 14);
            }

            @Override
            public boolean click(double mouseX, double mouseY, int button) {
                toggleFilters();
                return true;
            }

            @Override
            public List<Component> getTooltipLines() {
                return List.of(
                        filtersShown ? Texts.CLOSE_FILTERS_PANEL : Texts.OPEN_FILTERS_PANEL
                );
            }
        });

        var redstoneManager = new RedstoneManager<>(menu.getUpgrade().getOrDefault(MoreFunctionalStorage.REDSTONE_ACTION, RedstoneAction.IGNORE), true) {
            @Override
            public void setAction(RedstoneAction action) {
                menu.getUpgrade().set(MoreFunctionalStorage.REDSTONE_ACTION, action);
                menu.notifyRebuild();
            }

            @Override
            public RedstoneAction getAction() {
                return menu.getUpgrade().getOrDefault(MoreFunctionalStorage.REDSTONE_ACTION, RedstoneAction.IGNORE);
            }
        };

        var component = new RedstoneControlButtonComponent<>(
                153 - 2 * 18, 28 + 2 * 14 + 6, 14, 14,
                () -> redstoneManager,
                () -> menu.drawer
        );

        getAddons().add(new StateButtonAddon(component, new StateButtonInfo(0, AssetTypes.BUTTON_REDSTONE_IGNORED, "tooltip.titanium.redstone.ignored"), new StateButtonInfo(1, AssetTypes.BUTTON_REDSTONE_NO_REDSTONE, "tooltip.titanium.redstone.no_redstone"), new StateButtonInfo(2, AssetTypes.BUTTON_REDSTONE_REDSTONE, "tooltip.titanium.redstone.redstone"), new StateButtonInfo(3, AssetTypes.BUTTON_REDSTONE_ONCE, "tooltip.titanium.redstone.once")) {
            @Override
            public int getState() {
                return redstoneManager.getAction().getValues().indexOf(redstoneManager.getAction());
            }

            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (!this.isMouseOver(mouseX - getGuiLeft(), mouseY - getGuiTop())) {
                    return false;
                }

                Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.0F, RandomSource.create(), Minecraft.getInstance().player.blockPosition()));

                int next = menu.getUpgrade().getOrDefault(MoreFunctionalStorage.REDSTONE_ACTION, RedstoneAction.IGNORE).ordinal() + 1;
                menu.getUpgrade().set(MoreFunctionalStorage.REDSTONE_ACTION, RedstoneAction.values()[next >= RedstoneAction.values().length ? 0 : next]);
                menu.notifyRebuild();

                return true;
            }
        });
    }

    protected void toggleFilters() {
        if (filtersShown) {
            getAddons().remove(filterAddon);
            getAddons().removeAll(filterAddon.addons);
            filtersShown = false;
        } else {
            getAddons().add(filterAddon);
            getAddons().addAll(filterAddon.addons);
            filtersShown = true;
        }
    }

    public boolean displayingFilters() {
        return filtersShown;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ESCAPE || (keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue() && !isFocused()))) {
            // Intercept ESC/E and immediately reopen the router GUI - this avoids an
            // annoying screen flicker between closing the module GUI and reopen the router GUI.
            // Sending the reopen message will also close this gui, triggering onGuiClosed()
            PacketDistributor.sendToServer(new OpenDrawerMenuPayload(menu.drawer.getBlockPos()));
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
