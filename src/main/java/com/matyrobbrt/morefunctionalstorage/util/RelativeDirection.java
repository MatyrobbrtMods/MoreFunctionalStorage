package com.matyrobbrt.morefunctionalstorage.util;

import com.buuz135.functionalstorage.block.Drawer;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
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

    public Direction getAbsolute(ControllableDrawerTile<?> drawer) {
        var facing = drawer.getFacingDirection();
        if (facing.getAxis() == Direction.Axis.Y) {
            var orientated = drawer.getBlockState().getOptionalValue(Drawer.FACING_ALL).orElse(Direction.NORTH);
            return switch (this) {
                case FRONT -> facing;
                case BACK -> facing.getOpposite();

                case UP -> orientated;
                case DOWN -> orientated.getOpposite();

                case LEFT -> orientated.getCounterClockWise();
                case RIGHT -> orientated.getClockWise();
            };
        }

        return switch (this) {
            case FRONT -> facing;
            case BACK -> facing.getOpposite();

            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
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
