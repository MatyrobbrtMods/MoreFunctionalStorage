package com.matyrobbrt.morefunctionalstorage.item;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.matyrobbrt.morefunctionalstorage.MFSConfig;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.attach.UpgradeDataManager;
import com.matyrobbrt.morefunctionalstorage.menu.PlacerUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class PlacerUpgrade extends MFSUpgrade {
    public PlacerUpgrade(Item.Properties properties) {
        super(properties, PlacerUpgradeMenu::new);
    }

    @Override
    protected boolean hasOwner() {
        return true;
    }

    @Override
    protected Component getInfoTooltip() {
        return Texts.PLACER_UPGRADE;
    }

    @Override
    protected int getBaseSpeed() {
        return MFSConfig.BASE_PLACER_SPEED.getAsInt();
    }

    @Override
    protected void doWork(Level level, BlockPos pos, ItemStack upgradeStack, ControllableDrawerTile<?> drawer, UpgradeDataManager data) {
        var dir = getDirection(drawer, upgradeStack);
        var target = pos.relative(dir);
        if (!level.getBlockState(target).canBeReplaced()) return;

        var filter = upgradeStack.get(MoreFunctionalStorage.FILTER);

        var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, dir.getOpposite());
        if (cap == null) return;

        var selectedSlots = upgradeStack.get(MoreFunctionalStorage.SELECTED_SLOTS);
        if (selectedSlots == null) {
            for (int i = 0; i < cap.getSlots(); i++) {
                if (work(level, target, dir, cap, filter, i)) {
                    break;
                }
            }
        } else {
            for (int selectedSlot : selectedSlots) {
                if (work(level, target, dir, cap, filter, selectedSlot)) {
                    break;
                }
            }
        }
    }

    private boolean work(Level level, BlockPos target, Direction direction, IItemHandler drawer, @Nullable FilterConfiguration filter, int slot) {
        var stack = drawer.extractItem(slot, 1, true);
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem bi) || (filter != null && !filter.test(stack))) return false;

        Player placingPlayer = getFakePlayer(level, stack);

        var ctx = new BlockPlaceContext(
                level, placingPlayer, InteractionHand.MAIN_HAND, stack,
                new BlockHitResult(target.getBottomCenter(), direction, target, false)
        );
        if (bi.place(ctx) != InteractionResult.FAIL) {
            drawer.extractItem(slot, 1, false);
            return true;
        }

        return false;
    }

}
