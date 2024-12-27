package com.matyrobbrt.morefunctionalstorage.mixin;

import net.neoforged.neoforge.items.SlotItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SlotItemHandler.class)
public interface SlotItemHandlerAccess {
    @Accessor("index")
    int morefuncstorage$getIndex();
}
