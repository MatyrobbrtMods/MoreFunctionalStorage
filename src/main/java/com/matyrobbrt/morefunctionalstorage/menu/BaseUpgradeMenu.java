package com.matyrobbrt.morefunctionalstorage.menu;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.hrznstudio.titanium.container.BasicAddonContainer;
import com.hrznstudio.titanium.network.locator.instance.InventoryStackLocatorInstance;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.packet.ReplaceUpgradePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class BaseUpgradeMenu extends BasicAddonContainer {
    public final ControllableDrawerTile<?> drawer;
    public ComponentItemHandler augmentHandler;
    private final int slot;
    protected BaseUpgradeMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory, BlockPos pos, int index) {
        super(playerInventory, new PositionedInventoryStackLocatorInstance("mfs_drawer_upgrade", index, pos), (MenuType)menuType, ContainerLevelAccess.create(playerInventory.player.level(), pos), playerInventory, containerId);

        this.slot = index;
        this.drawer = ((ControllableDrawerTile)playerInventory.player.level().getBlockEntity(pos));
    }

    @Override
    public void addExtraSlots() {
        // We can't use getUpgrade here because the super ctor calls this
        var loc = (PositionedInventoryStackLocatorInstance) getLocatorInstance();
        var drawer = ((ControllableDrawerTile)getPlayerInventory().player.level().getBlockEntity(loc.pos));
        addSlots(drawer, loc.inventorySlot);
    }

    protected void addSlots(ControllableDrawerTile<?> drawer, int upgradeSlot) {
        augmentHandler = new ComponentItemHandler(drawer.getUtilityUpgrades().getStackInSlot(upgradeSlot), MoreFunctionalStorage.AUGMENTS.get(), 1) {
            @Override
            public int getSlotLimit(int slot) {
                return 6;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.isEmpty() || stack.is(MoreFunctionalStorage.SPEED_UPGRADE_AUGMENT);
            }
        };
        addSlot(new ItemHandlerCopySlot(augmentHandler, 0, 152, 80));
    }

    public ItemStack getUpgrade() {
        return drawer.getUtilityUpgrades().getStackInSlot(slot);
    }

    public void notifyRebuild() {
        if (getPlayerInventory().player.level().isClientSide) {
            PacketDistributor.sendToServer(new ReplaceUpgradePayload(slot, getUpgrade().copy()));
        } else {
            throw null;
//            PacketDistributor.sendToPlayer((ServerPlayer) getPlayerInventory().player, new ReplaceUpgradePayload(slot, getUpgrade().copy()));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level() == drawer.getLevel();
    }

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
    public static class PositionedInventoryStackLocatorInstance extends InventoryStackLocatorInstance {
        private transient BlockPos pos;

        // Yes we have to copy those... no, LocatorInstance#toBytes has no concept of hierarchy
        private int inventorySlot;
        private String finder;

        public PositionedInventoryStackLocatorInstance(String finder, int inventorySlot, BlockPos pos) {
            super(finder, inventorySlot);
            this.pos = pos;

            this.finder = finder;
            this.inventorySlot = inventorySlot;
        }
    }
}
