package com.matyrobbrt.morefunctionalstorage.packet;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.hrznstudio.titanium.container.BasicAddonContainer;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.item.MFSUpgrade;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public record OpenUpgradeMenuPayload(int slot) implements CustomPacketPayload {
    public static final Type<OpenUpgradeMenuPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MoreFunctionalStorage.MOD_ID, "open_upgrade_menu"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenUpgradeMenuPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenUpgradeMenuPayload::slot,

            OpenUpgradeMenuPayload::new
    );

    public void handle(IPayloadContext context) {
        if (context.player().containerMenu instanceof BasicAddonContainer cont && cont.getProvider() instanceof ControllableDrawerTile<?> tile) {
            var upgrade = tile.getUtilityUpgrades().getStackInSlot(slot);
            if (upgrade.getItem() instanceof MFSUpgrade up) {
                context.player().openMenu(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return upgrade.getHoverName();
                    }

                    @Nullable
                    @Override
                    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                        return up.factory.create(containerId, playerInventory, tile.getBlockPos(), slot);
                    }

                    @Override
                    public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                        return false;
                    }
                }, buf -> {
                    buf.writeBlockPos(tile.getBlockPos());
                    buf.writeVarInt(slot);
                });
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
