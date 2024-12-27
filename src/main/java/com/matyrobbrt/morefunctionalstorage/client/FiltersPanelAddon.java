package com.matyrobbrt.morefunctionalstorage.client;

import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.util.AssetUtil;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.item.FilterConfiguration;
import com.matyrobbrt.morefunctionalstorage.menu.BaseUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public class FiltersPanelAddon extends BasicScreenAddon {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MoreFunctionalStorage.MOD_ID, "textures/gui/filters_panel.png");
    private static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath(MoreFunctionalStorage.MOD_ID, "textures/gui/filter_buttons.png");

    public final List<IScreenAddon> addons;
    private final BaseUpgradeMenu menu;
    public FiltersPanelAddon(int posX, int posY, BaseUpgradeMenu menu) {
        super(posX, posY);
        this.menu = menu;
        this.addons = List.of(
                new ButtonAddon(getPosX() + 3 + 3, getPosY() + 7 + 3 * 18 + 3, 14, 14) {
                    @Override
                    public boolean click(double mouseX, double mouseY, int button) {
                        var cfg = getConfig();
                        menu.getUpgrade().set(MoreFunctionalStorage.FILTER, cfg.withBlacklist(!cfg.blacklist()));
                        menu.notifyRebuild();
                        return true;
                    }

                    @Override
                    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float v) {
                        guiGraphics.blit(BUTTONS_TEXTURE, guiX + getPosX(), guiY + getPosY(), 28, getConfig().blacklist() ? 14 : 0, 14, 14, 42, 28);
                    }

                    @Override
                    public List<Component> getTooltipLines() {
                        var blacklist = getConfig().blacklist();
                        return List.of(Texts.WHITELIST_BLACKLIST.format(
                                blacklist ? Texts.BLACKLIST.format(ChatFormatting.GOLD) : Texts.WHITELIST.format(ChatFormatting.GOLD)
                        ), Texts.CLICK_TO_TOGGLE.format(blacklist ? Texts.WHITELIST : Texts.BLACKLIST));
                    }
                },
                new ButtonAddon(getPosX() + 3 + 3 + 14 + 3, getPosY() + 7 + 3 * 18 + 3, 14, 14) {
                    @Override
                    public boolean click(double mouseX, double mouseY, int button) {
                        var cfg = getConfig();
                        menu.getUpgrade().set(MoreFunctionalStorage.FILTER, cfg.withMatchComponents(!cfg.matchComponents()));
                        menu.notifyRebuild();
                        return true;
                    }

                    @Override
                    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float v) {
                        guiGraphics.blit(BUTTONS_TEXTURE, guiX + getPosX(), guiY + getPosY(), 14, getConfig().matchComponents() ? 14 : 0, 14, 14, 42, 28);
                    }

                    @Override
                    public List<Component> getTooltipLines() {
                        var enabled = getConfig().matchComponents();
                        return List.of(Texts.COMPONENT_MATCHING.format(
                                enabled ? Texts.ENABLED.format(ChatFormatting.GOLD) : Texts.DISABLED.format(ChatFormatting.GOLD)
                        ), Texts.CLICK_TO.format(enabled ? Texts.DISABLE : Texts.ENABLE));
                    }
                },
                new ButtonAddon(getPosX() + 3 + 3 + 14 * 2 + 3 * 2, getPosY() + 7 + 3 * 18 + 3, 14, 14) {
                    @Override
                    public boolean click(double mouseX, double mouseY, int button) {
                        var cfg = getConfig();
                        menu.getUpgrade().set(MoreFunctionalStorage.FILTER, cfg.withMatchTags(!cfg.matchTags()));
                        menu.notifyRebuild();
                        return true;
                    }

                    @Override
                    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float v) {
                        guiGraphics.blit(BUTTONS_TEXTURE, guiX + getPosX(), guiY + getPosY(), 0, getConfig().matchTags() ? 14 : 0, 14, 14, 42, 28);
                    }

                    @Override
                    public List<Component> getTooltipLines() {
                        var enabled = getConfig().matchTags();
                        return List.of(Texts.TAG_MATCHING.format(
                                enabled ? Texts.ENABLED.format(ChatFormatting.GOLD) : Texts.DISABLED.format(ChatFormatting.GOLD)
                        ), Texts.CLICK_TO.format(enabled ? Texts.DISABLE : Texts.ENABLE));
                    }
                }
        );
    }

    private FilterConfiguration getConfig() {
        return menu.getUpgrade().getOrDefault(MoreFunctionalStorage.FILTER, FilterConfiguration.EMPTY);
    }

    @Override
    public int getXSize() {
        return 63;
    }

    @Override
    public int getYSize() {
        return 84;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        var config = menu.getUpgrade().getOrDefault(MoreFunctionalStorage.FILTER, FilterConfiguration.EMPTY);
        var filters = config.filters();

        guiGraphics.blit(TEXTURE, guiX + getPosX(), guiY + getPosY(), 0, 0, getXSize(), getYSize(), getXSize(), getYSize());

        guiY += 3;

        var colour = (128 << 24) | DyeColor.LIME.getTextColor();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int x = guiX + getPosX() + 3 + j * 18;
                int y = guiY + getPosY() + 4 + i * 18;
                AssetUtil.drawAsset(guiGraphics, screen, provider.getAsset(AssetTypes.SLOT), x, y);

                guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, colour);

                RenderSystem.setShaderColor(1, 1, 1, 1);
                var stack = filters.get(i * 3 + j).stack();
                if (!stack.isEmpty()) {
                    Lighting.setupFor3DItems();
                    guiGraphics.renderItem(stack, x + 1, y + 1);
                }
            }
        }
    }

    public int getSlotUnderMouse(double mouseX, double mouseY) {
        var screen = (AbstractContainerScreen) Minecraft.getInstance().screen;

        mouseX -= screen.getGuiLeft();
        mouseX -= getPosX();

        mouseY -= screen.getGuiTop();
        mouseY -= getPosY();

        if (mouseX >= 3 && mouseY >= 7 && mouseX <= 3 + 3 * 18 && mouseY <= 7 + 3 * 18) {
            return ((int)(Math.max(mouseY - 7 - 1, 0)) / 18) * 3 + ((int)(Math.max(mouseX - 3 - 1, 0)) / 18);
        }

        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var screen = (AbstractContainerScreen) Minecraft.getInstance().screen;
        var slot = getSlotUnderMouse(mouseX, mouseY);
        if (slot >= 0) {
            var carried = screen.getMenu().getCarried();
            var config = menu.getUpgrade().getOrDefault(MoreFunctionalStorage.FILTER, FilterConfiguration.EMPTY);

            if (carried.isEmpty()) {
                var cfg = config.withItemInSlot(slot, ItemStack.EMPTY, null);
                if (cfg.filters().stream().allMatch(FilterConfiguration.FilterStack::isEmpty) && !cfg.blacklist() && !cfg.matchTags() && !cfg.matchComponents()) {
                    menu.getUpgrade().remove(MoreFunctionalStorage.FILTER);
                } else {
                    menu.getUpgrade().set(MoreFunctionalStorage.FILTER, cfg);
                }
            } else {
                menu.getUpgrade().set(MoreFunctionalStorage.FILTER, config.withItemInSlot(slot, carried.copyWithCount(1), null));
            }

            menu.notifyRebuild();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        var slot = getSlotUnderMouse(mouseX, mouseY);
        if (slot < 0) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        var config = getConfig();
        if (!config.matchTags()) return false;
        var stack = config.filters().get(slot);
        if (stack.isEmpty()) return false;

        var tags = stack.stack().getTags()
                .sorted(Comparator.comparing(TagKey::location))
                .toList();

        var newTag = stack.tag().orElse(null);
        int extremity = scrollY > 0 ? tags.size() - 1 : 0;
        int direction = scrollY > 0 ? -1 : +1;

        if (newTag == null) {
            if (!tags.isEmpty()) {
                newTag = tags.get(extremity);
            }
        } else {
            var idx = tags.indexOf(newTag);
            if (idx < 0) {
                if (!tags.isEmpty()) {
                    newTag = tags.get(extremity);
                }
            } else {
                newTag = idx == (extremity == 0 ? tags.size() - 1 : 0) ? null : tags.get(idx + direction);
            }
        }

        menu.getUpgrade().set(MoreFunctionalStorage.FILTER, config.withItemInSlot(slot, stack.stack(), newTag));
        menu.notifyRebuild();
        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        var screen = (AbstractContainerScreen) Minecraft.getInstance().screen;
        mouseX -= screen.getGuiLeft();
        mouseY -= screen.getGuiTop();

        return mouseX > (double)this.getPosX() && mouseX < (double)(this.getPosX() + this.getXSize()) && mouseY > (double)this.getPosY() && mouseY < (double)(this.getPosY() + this.getYSize());
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        var slot = getSlotUnderMouse(mouseX, mouseY);
        if (slot >= 0 && menu.getCarried().isEmpty()) {
            var config = getConfig();
            var stack = config.filters().get(slot);
            if (!stack.isEmpty()) {
                var tooltip = Screen.getTooltipFromItem(screen.getMinecraft(), stack.stack());

                if (config.matchTags()) {
                    var currentTag = stack.tag().orElse(null);

                    tooltip.add(Texts.TAG_SELECTION);

                    stack.stack().getItemHolder().tags().sorted(Comparator.comparing(TagKey::location)).forEach(tag -> tooltip.add(createTagComponent(
                            Component.literal(tag.location().toString()),
                            currentTag == tag
                    )));

                    tooltip.add(createTagComponent(Texts.ANY_OF_THE_ABOVE.format(ChatFormatting.ITALIC), currentTag == null));
                }

                guiGraphics.renderTooltip(
                        screen.getMinecraft().font,
                        tooltip, stack.stack().getTooltipImage(),
                        stack.stack(), mouseX - guiX, mouseY - guiY
                );
            }
        }
    }

    private Component createTagComponent(MutableComponent text, boolean selected) {
        var comp = Component.literal("‣ ");
        if (selected) {
            text = text.withStyle(ChatFormatting.AQUA)
                    .append(" (").append(Texts.SELECTED).append(")");
        }
        return comp.append(text);
    }
}
