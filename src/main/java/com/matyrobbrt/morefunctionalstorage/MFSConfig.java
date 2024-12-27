package com.matyrobbrt.morefunctionalstorage;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MFSConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue SPEED_AUGMENT_REDUCTION = BUILDER
            .comment("The reduction of ticks/process each speed augment provides")
            .defineInRange("speedAugmentReduction", 10, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue BASE_PLACER_SPEED = BUILDER
            .comment("The base speed (in ticks) of the Placer Upgrade")
            .defineInRange("basePlacerSpeed", 60, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue BASE_BREAKER_SPEED = BUILDER
            .comment("The base speed (in ticks) of the Breaker Upgrade")
            .defineInRange("baseBreakerSpeed", 60, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
