package com.matyrobbrt.morefunctionalstorage.item;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.matyrobbrt.morefunctionalstorage.MFSConfig;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.attach.UpgradeDataManager;
import com.matyrobbrt.morefunctionalstorage.menu.StonecuttingUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StonecuttingUpgrade extends MFSUpgrade {
    private static final Predicate<ItemStack> TRUE = s -> true;

    public StonecuttingUpgrade(Properties properties) {
        super(properties, StonecuttingUpgradeMenu::new);
    }

    @Override
    public boolean hasDirection() {
        return false;
    }

    @Override
    protected boolean hasOwner() {
        return false;
    }

    @Override
    protected int getBaseSpeed() {
        return MFSConfig.BASE_STONECUTTING_SPEED.getAsInt();
    }

    @Override
    protected Component getInfoTooltip() {
        return Texts.STONECUTTING_UPGRADE;
    }

    @Override
    protected void doWork(Level level, BlockPos pos, ItemStack upgradeStack, ControllableDrawerTile<?> drawer, UpgradeDataManager data) {
        var filter = Objects.requireNonNullElse(upgradeStack.get(MoreFunctionalStorage.FILTER), TRUE);

        var slots = getEffectiveSlots(upgradeStack, drawer);

        var cap = drawer.getItemHandler(null);
        for (int i : slots) {
            var stack = cap.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            var input = new SingleRecipeInput(stack);

            var recipe = getRecipesFor(level, RecipeType.STONECUTTING, input)
                    .filter(filter)
                    .filter(output -> {
                        output = output.copy();
                        for (int slot : slots) {
                            if (slot != i && cap.insertItem(slot, output, true).isEmpty() && cap.extractItem(i, 1, true).getCount() == 1) {
                                cap.insertItem(slot, output, false);
                                cap.extractItem(i, 1, false);
                                return true;
                            }
                        }
                        return false;
                    })
                    .findFirst()
                    .orElse(null);
            if (recipe != null) return;
        }
    }

    private static <I extends RecipeInput, T extends Recipe<I>> Stream<ItemStack> getRecipesFor(Level level, RecipeType<T> recipeType, I input) {
        return level.getRecipeManager().byType(recipeType)
                .stream()
                .filter(rec -> rec.value().matches(input, level))
                .map(rec -> rec.value().getResultItem(level.registryAccess()))
                .sorted(Comparator.comparing(ItemStack::getDescriptionId));
    }
}
