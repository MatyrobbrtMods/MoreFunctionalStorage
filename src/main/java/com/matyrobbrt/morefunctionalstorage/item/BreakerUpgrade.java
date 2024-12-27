package com.matyrobbrt.morefunctionalstorage.item;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.matyrobbrt.morefunctionalstorage.MFSConfig;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.attach.UpgradeDataManager;
import com.matyrobbrt.morefunctionalstorage.client.MFSClient;
import com.matyrobbrt.morefunctionalstorage.menu.BreakerUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class BreakerUpgrade extends MFSUpgrade {
    public BreakerUpgrade(Properties properties) {
        super(properties, BreakerUpgradeMenu::new);
    }

    @Override
    protected boolean hasOwner() {
        return true;
    }

    @Override
    protected int getBaseSpeed() {
        return MFSConfig.BASE_BREAKER_SPEED.getAsInt();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // If we have ticked the item (so it's not inside JEI) we will display a warning
        if (stack.has(MoreFunctionalStorage.OWNER)) {
            var tool = stack.getOrDefault(MoreFunctionalStorage.TOOL, ItemContainerContents.EMPTY)
                    .nonEmptyStream().findFirst().orElse(null);
            if (tool == null) {
                tooltip.add(Texts.NO_TOOL_INSTALLED);
            } else {
                tooltip.add(Texts.TOOL.format(tool.getHoverName().copy().withStyle(ChatFormatting.LIGHT_PURPLE)));
            }
        }

        if (FMLEnvironment.dist.isClient() && MFSClient.isInsideDrawerUI()) {
            tooltip.add(Texts.RCLICK_CONFIGURE.format(ChatFormatting.ITALIC));
        } else {
            if (!flagIn.hasShiftDown()) {
                tooltip.add(Texts.HOLD_SHIFT);
            } else {
                tooltip.add(Texts.BREAKER_UPGRADE);
            }
        }
    }

    @Override
    protected void doWork(Level level, BlockPos drawerPos, ItemStack upgradeStack, ControllableDrawerTile<?> drawer, UpgradeDataManager data) {
        var toolContents = upgradeStack.get(MoreFunctionalStorage.TOOL);
        if (toolContents == null) return;
        var tool = toolContents.nonEmptyStream().findFirst().orElse(null);
        if (tool == null) return;

        var dir = getDirection(level.getBlockState(drawerPos).getValue(RotatableBlock.FACING_HORIZONTAL), upgradeStack);
        var target = drawerPos.relative(dir);
        if (level.getBlockState(target).isEmpty()) return;

        var filter = upgradeStack.get(MoreFunctionalStorage.FILTER);

        var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, drawerPos, dir.getOpposite());
        if (cap == null) return;

        var player = getFakePlayer(level, upgradeStack);

        BlockState blockstate = level.getBlockState(target);
        if (!tool.isCorrectToolForDrops(blockstate)) return;

        LootParams.Builder lootParams = new LootParams.Builder((ServerLevel) level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(target))
                .withParameter(LootContextParams.BLOCK_STATE, blockstate)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(target))
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.TOOL, tool);

        List<ItemStack> drops = blockstate.getDrops(lootParams);
        if (filter != null && !drops.stream().allMatch(filter)) return;

        var selectedSlots = upgradeStack.get(MoreFunctionalStorage.SELECTED_SLOTS);
        var actions = new ArrayList<Runnable>(selectedSlots == null ? cap.getSlots() : selectedSlots.size());

        if (selectedSlots == null) {
            for (int i = 0; i < cap.getSlots(); i++) {
                attemptInsert(cap, i, drops, actions);
            }
        } else {
            for (int selectedSlot : selectedSlots) {
                attemptInsert(cap, selectedSlot, drops, actions);
            }
        }

        if (drops.isEmpty()) {
            if (player != null && CommonHooks.fireBlockBreak(level, GameType.CREATIVE, player, target, blockstate).isCanceled()) {
                return;
            }

            actions.forEach(Runnable::run);
            level.destroyBlock(target, false, player);
        }
    }

    private void attemptInsert(IItemHandler drawer, int slot, List<ItemStack> drops, List<Runnable> finishActions) {
        var itr = drops.iterator();
        while (itr.hasNext()) {
            var drop = itr.next();
            var remainder = drawer.insertItem(slot, drop, true);
            if (remainder.getCount() < drop.getCount()) {
                var copiedDrop = drop.copyWithCount(drop.getCount() - remainder.getCount());
                finishActions.add(() -> drawer.insertItem(slot, copiedDrop, false));
                drop.setCount(remainder.getCount());
                if (drop.isEmpty()) {
                    itr.remove();
                }
                break;
            }
        }
    }
}
