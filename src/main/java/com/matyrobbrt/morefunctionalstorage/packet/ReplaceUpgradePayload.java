package com.matyrobbrt.morefunctionalstorage.packet;

import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.menu.BaseUpgradeMenu;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReplaceUpgradePayload(int slot, ItemStack upgrade) implements CustomPacketPayload {
    public static final Type<ReplaceUpgradePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MoreFunctionalStorage.MOD_ID, "replace_upgrade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReplaceUpgradePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ReplaceUpgradePayload::slot,
            ItemStack.STREAM_CODEC, ReplaceUpgradePayload::upgrade,
            ReplaceUpgradePayload::new
    );

    public void handle(IPayloadContext context) {
        if (context.player().containerMenu instanceof BaseUpgradeMenu cont) {
            var inSlot = cont.drawer.getUtilityUpgrades().getStackInSlot(slot);
            if (inSlot.getItem() == upgrade.getItem()) {
                ((PatchedDataComponentMap)inSlot.getComponents()).restorePatch(upgrade.getComponentsPatch());
                cont.drawer.markForUpdate();
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
