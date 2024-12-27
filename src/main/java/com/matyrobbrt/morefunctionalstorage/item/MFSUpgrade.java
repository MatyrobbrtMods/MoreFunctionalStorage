package com.matyrobbrt.morefunctionalstorage.item;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.item.FunctionalUpgradeItem;
import com.hrznstudio.titanium.api.redstone.IRedstoneState;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.block.redstone.RedstoneAction;
import com.hrznstudio.titanium.block.redstone.RedstoneState;
import com.matyrobbrt.morefunctionalstorage.MFSConfig;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.menu.BaseUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.util.RelativeDirection;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MFSUpgrade extends FunctionalUpgradeItem {
    public final MenuFactory factory;

    protected MFSUpgrade(Properties properties, MenuFactory factory) {
        super(properties);
        this.factory = factory;
    }

    protected boolean hasOwner() {
        return false;
    }

    protected int getBaseSpeed() {
        return 1;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (hasOwner() && !level.isClientSide() && !stack.has(MoreFunctionalStorage.OWNER) && entity instanceof Player pl) {
            stack.set(MoreFunctionalStorage.OWNER, pl.getGameProfile());
            stack.set(MoreFunctionalStorage.RELATIVE_DIRECTION, RelativeDirection.FRONT);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Texts.UPGRADE_SPEED.format(
                Component.literal(String.valueOf(getSpeed(stack))).withStyle(ChatFormatting.GOLD)
        ));

        if (stack.has(MoreFunctionalStorage.RELATIVE_DIRECTION)) {
            tooltip.add(Texts.DIRECTION_SIMPLE.format(
                    stack.get(MoreFunctionalStorage.RELATIVE_DIRECTION).getDisplayText().withStyle(ChatFormatting.AQUA)
            ));
        }

        if (stack.has(MoreFunctionalStorage.FILTER)) {
            tooltip.add(Texts.HAS_FILTERS_CONFIGURED);
        }
    }

    protected abstract void doWork(Level level, BlockPos pos, ItemStack upgradeStack, ControllableDrawerTile<?> drawer);

    @Override
    public final void work(Level level, BlockPos pos, ItemStack upgradeStack) {
        int speed = getSpeed(upgradeStack);

        var action = upgradeStack.getOrDefault(MoreFunctionalStorage.REDSTONE_ACTION, RedstoneAction.IGNORE);

        if (action == RedstoneAction.ONCE || level.getGameTime() % speed == 0) {
            if (level.getBlockEntity(pos) instanceof ControllableDrawerTile<?> be) {
                var data = be.getData(MoreFunctionalStorage.UPGRADE_DATA_MANAGER).getManager(upgradeStack);
                var lastRedstone = data.bool("lastRedstone");

                var currentRedstone = getRedstone(level, pos);

                boolean work;
                if (action.startsOnChange()) {
                    work = currentRedstone.isReceivingRedstone() != lastRedstone.get() && currentRedstone.isReceivingRedstone();
                } else {
                    work = action.canRun(currentRedstone);
                }
                lastRedstone.set(currentRedstone.isReceivingRedstone());

                if (work) {
                    doWork(level, pos, upgradeStack, be);
                }
            }
        }
    }

    @Nullable
    protected ServerPlayer getFakePlayer(Level level, ItemStack upgradeStack) {
        var owner = upgradeStack.get(MoreFunctionalStorage.OWNER);
        if (owner != null) {
            return FakePlayerFactory.get((ServerLevel) level, new GameProfile(owner.getId(), "[MoreFunctionalStorage]" + owner.getName()));
        }
        return null;
    }

    public static int getSpeed(ItemStack upgradeStack) {
        var component = upgradeStack.get(MoreFunctionalStorage.AUGMENTS);
        return Math.max(((MFSUpgrade) upgradeStack.getItem()).getBaseSpeed() - ((component == null ||
                component.getSlots() < 1) ? 0 : component.getStackInSlot(0).getCount() * MFSConfig.SPEED_AUGMENT_REDUCTION.getAsInt()), 1);
    }

    public static Direction getDirection(Direction facing, ItemStack stack) {
        return stack.getOrDefault(MoreFunctionalStorage.RELATIVE_DIRECTION, RelativeDirection.FRONT).getAbsolute(facing);
    }

    public static IRedstoneState getRedstone(Level level, BlockPos pos) {
        return level.getBestNeighborSignal(pos) > 0 ? RedstoneState.ON : RedstoneState.OFF;
    }

    public interface MenuFactory {
        BaseUpgradeMenu create(int containerId, Inventory playerInventory, BlockPos pos, int index);
    }
}