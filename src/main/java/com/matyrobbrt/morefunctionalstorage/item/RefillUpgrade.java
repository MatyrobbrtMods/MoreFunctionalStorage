package com.matyrobbrt.morefunctionalstorage.item;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.matyrobbrt.morefunctionalstorage.MFSConfig;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.attach.UpgradeDataManager;
import com.matyrobbrt.morefunctionalstorage.client.MFSClient;
import com.matyrobbrt.morefunctionalstorage.menu.RefillUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class RefillUpgrade extends MFSUpgrade {

    private final boolean dimensional;
    public RefillUpgrade(Properties properties, boolean dimensional) {
        super(properties, RefillUpgradeMenu::new);
        this.dimensional = dimensional;
    }

    @Override
    protected int getBaseSpeed() {
        return dimensional ? MFSConfig.BASE_DIMENSIONAL_REFILL_SPEED.getAsInt() : MFSConfig.BASE_REFILL_SPEED.getAsInt();
    }

    @Override
    protected boolean hasOwner() {
        return true;
    }

    @Override
    public boolean hasDirection() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (FMLEnvironment.dist.isClient() && MFSClient.isInsideDrawerUI(stack)) {
            tooltip.add(Component.empty());
            tooltip.add(Texts.RCLICK_CONFIGURE.format(ChatFormatting.GRAY));
        } else {
            if (!isShiftDown(flagIn)) {
                tooltip.add(Component.empty());
                tooltip.add(Texts.HOLD_SHIFT);
            } else {
                tooltip.add(Texts.REFILL_UPGRADE);
            }
        }
    }

    @Override
    protected void doWork(Level level, BlockPos pos, ItemStack upgradeStack, ControllableDrawerTile<?> drawer, UpgradeDataManager data) {
        var ownerId = upgradeStack.get(MoreFunctionalStorage.OWNER);
        if (ownerId == null) return;

        var owner = level.getServer().getPlayerList().getPlayer(ownerId.getId());
        if (owner == null || (!dimensional && owner.level() != level)) return;

        var target = upgradeStack.getOrDefault(MoreFunctionalStorage.REFILL_TARGET, RefillTarget.HOTBAR);

        var inv = data.getCached("refill_inv", () -> target.get(owner), target, ownerId.getId());
        var filter = upgradeStack.get(MoreFunctionalStorage.FILTER);

        var cap = drawer.getItemHandler(null);
        if (cap == null) return;

        var selectedSlots = upgradeStack.get(MoreFunctionalStorage.SELECTED_SLOTS);

        outer: for (int i = 0; i < inv.getSlots(); i++) {
            var stack = inv.getStackInSlot(i);
            if (stack.isEmpty() || (filter != null && !filter.test(stack)) || stack.getCount() >= stack.getMaxStackSize()) continue;

            if (selectedSlots == null) {
                for (int selectedSlot = 0; selectedSlot < cap.getSlots(); selectedSlot++) {
                    var item = cap.extractItem(selectedSlot, 1, true);
                    if (!item.isEmpty() && ItemStack.isSameItemSameComponents(item, stack)) {
                        stack.grow(1);
                        cap.extractItem(selectedSlot, 1, false);
                        break outer;
                    }
                }
            } else {
                for (int selectedSlot : selectedSlots) {
                    var item = cap.extractItem(selectedSlot, 1, true);
                    if (!item.isEmpty() && ItemStack.isSameItemSameComponents(item, stack)) {
                        stack.grow(1);
                        cap.extractItem(selectedSlot, 1, false);
                        break outer;
                    }
                }
            }
        }
    }

    public enum RefillTarget implements StringRepresentable {
        HOTBAR(pl -> new RangedWrapper(new InvWrapper(pl.getInventory()), 0, Inventory.getSelectionSize()), Texts.REFILL_HOTBAR),
        INVENTORY(pl -> new RangedWrapper(new InvWrapper(pl.getInventory()), Inventory.getSelectionSize(), pl.getInventory().items.size()), Texts.REFILL_MAIN_INV),
        ENDER_CHEST(pl -> new InvWrapper(pl.getEnderChestInventory()), Texts.REFILL_ENDER_CHEST);

        public static final Codec<RefillTarget> CODEC = StringRepresentable.fromEnum(RefillTarget::values);
        public static final StreamCodec<ByteBuf, RefillTarget> STREAM_CODEC = ByteBufCodecs.idMapper(
                i -> RefillTarget.values()[i], RefillTarget::ordinal
        );

        private final String name;
        private final Function<Player, IItemHandler> invGetter;
        public final Component displayText;
        RefillTarget(Function<Player, IItemHandler> invGetter, Component displayText) {
            this.displayText = displayText;
            this.name = name().toLowerCase(Locale.ROOT);
            this.invGetter = invGetter;
        }

        public IItemHandler get(Player player) {
            return invGetter.apply(player);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
