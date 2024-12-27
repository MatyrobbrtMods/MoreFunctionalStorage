package com.matyrobbrt.morefunctionalstorage.jei;

import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.client.screen.BaseUpgradeScreen;
import com.matyrobbrt.morefunctionalstorage.item.FilterConfiguration;
import com.matyrobbrt.morefunctionalstorage.menu.BaseUpgradeMenu;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class MFSJei implements IModPlugin {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MoreFunctionalStorage.MOD_ID, "jei");

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(BaseUpgradeScreen.class, new IGhostIngredientHandler<>() {
            @Override
            public <I> List<Target<I>> getTargetsTyped(BaseUpgradeScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
                List<Target<I>> targets = new ArrayList<>();
                if (gui.displayingFilters() && ingredient.getType() == VanillaTypes.ITEM_STACK) {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            int x = gui.getGuiLeft() + 176 + 3 + j * 18;
                            int y = gui.getGuiTop() + 10 + 7 + i * 18;
                            targets.add((Target<I>) new FilterTarget((BaseUpgradeMenu) gui.getMenu(), new Rect2i(x, y, 18, 18), i * 3 + j));
                        }
                    }
                }
                return targets;
            }

            @Override
            public void onComplete() {

            }

            record FilterTarget(BaseUpgradeMenu menu, Rect2i area, int slot) implements Target<ItemStack> {
                @Override
                public Rect2i getArea() {
                    return FilterTarget.this.area;
                }

                @Override
                public void accept(ItemStack ingredient) {
                    var cfg = FilterTarget.this.menu.getUpgrade().getOrDefault(MoreFunctionalStorage.FILTER, FilterConfiguration.EMPTY)
                            .withItemInSlot(FilterTarget.this.slot, ingredient, null);
                    menu().getUpgrade().set(MoreFunctionalStorage.FILTER, cfg);
                    menu().notifyRebuild();
                }
            }
        });
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
}
