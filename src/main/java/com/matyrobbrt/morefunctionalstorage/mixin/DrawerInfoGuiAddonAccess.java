package com.matyrobbrt.morefunctionalstorage.mixin;

import com.buuz135.functionalstorage.client.gui.DrawerInfoGuiAddon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(DrawerInfoGuiAddon.class)
public interface DrawerInfoGuiAddonAccess {
    @Accessor("gui")
    ResourceLocation mfs$getGui();
    @Accessor("slotAmount")
    int mfs$getSlotAmount();
    @Accessor("slotPosition")
    Function<Integer, Pair<Integer, Integer>> mfs$getSlotPosition();
    @Accessor("slotStack")
    Function<Integer, ItemStack> mfs$getSlotStack();
    @Accessor("slotMaxAmount")
    Function<Integer, Integer> mfs$getSlotMaxAmount();
}
