package com.matyrobbrt.morefunctionalstorage.packet;

import com.hrznstudio.titanium.block.tile.ActiveTile;
import com.hrznstudio.titanium.network.locator.LocatorFactory;
import com.hrznstudio.titanium.network.locator.instance.TileEntityLocatorInstance;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public record OpenDrawerMenuPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenDrawerMenuPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MoreFunctionalStorage.MOD_ID, "open_drawer_menu"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDrawerMenuPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenDrawerMenuPayload::pos,

            OpenDrawerMenuPayload::new
    );

    public void handle(IPayloadContext context) {
        if (context.player().level().getBlockEntity(pos) instanceof ActiveTile<?> mp) {
            context.player().openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return mp.getDisplayName();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    return mp.createMenu(containerId, playerInventory, player);
                }

                @Override
                public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                    return false;
                }
            }, (buffer) -> LocatorFactory.writePacketBuffer(buffer, new TileEntityLocatorInstance(mp.getBlockPos())));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
