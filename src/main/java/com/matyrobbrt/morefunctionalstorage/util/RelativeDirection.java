package com.matyrobbrt.morefunctionalstorage.util;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum RelativeDirection implements StringRepresentable {
    FRONT,
    BACK,
    LEFT,
    RIGHT,
    UP,
    DOWN;

    private final String serialized;

    RelativeDirection() {
        this.serialized = name().toLowerCase(Locale.ROOT);
    }

    public Direction getAbsolute(Direction facing) {
        return switch (this) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case FRONT -> facing;
            case BACK -> facing.getOpposite();
            case LEFT -> facing.getClockWise();
            case RIGHT -> facing.getCounterClockWise();
        };
    }

    @Override
    public String getSerializedName() {
        return serialized;
    }

    public MutableComponent getDisplayText() {
        return Component.translatable("drawer_upgrade.functionalstorage.sides." + serialized);
    }
}
