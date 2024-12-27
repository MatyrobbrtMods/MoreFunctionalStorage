package com.matyrobbrt.morefunctionalstorage.data;

import com.buuz135.functionalstorage.data.FunctionalStorageItemTagsProvider;
import com.buuz135.functionalstorage.util.StorageTags;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class MFSRecipe extends RecipeProvider {
    public MFSRecipe(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output, HolderLookup.Provider holderLookup) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MoreFunctionalStorage.PLACER_UPGRADE.toStack())
                .pattern("RdR")
                .pattern("dDd")
                .pattern("RtR")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('D', StorageTags.DRAWER)
                .define('d', Items.DISPENSER)
                .define('t', ItemTags.DIRT)
                .unlockedBy("has_drawer", has(StorageTags.DRAWER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MoreFunctionalStorage.BREAKER_UPGRADE.toStack())
                .pattern("RSR")
                .pattern("SDS")
                .pattern("RPR")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('D', StorageTags.DRAWER)
                .define('P', Items.IRON_PICKAXE)
                .define('S', ItemTags.STONE_BRICKS)
                .unlockedBy("has_drawer", has(StorageTags.DRAWER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MoreFunctionalStorage.REFILL_UPGRADE.toStack())
                .pattern("RPR")
                .pattern("PDP")
                .pattern("RCR")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('D', StorageTags.DRAWER)
                .define('P', Tags.Items.ENDER_PEARLS)
                .define('C', Tags.Items.CHESTS)
                .unlockedBy("has_drawer", has(StorageTags.DRAWER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MoreFunctionalStorage.DIMENSIONAL_REFILL_UPGRADE.toStack())
                .pattern(" W ")
                .pattern(" U ")
                .pattern(" P ")
                .define('W', Items.WITHER_SKELETON_SKULL)
                .define('U', MoreFunctionalStorage.REFILL_UPGRADE)
                .define('P', Items.ENDER_EYE)
                .unlockedBy("has_drawer", has(StorageTags.DRAWER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MoreFunctionalStorage.SPEED_UPGRADE_AUGMENT.toStack(2))
                .pattern("RBR")
                .pattern("BDB")
                .pattern("RBR")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('D', StorageTags.DRAWER)
                .define('B', Items.BLAZE_POWDER)
                .unlockedBy("has_drawer", has(StorageTags.DRAWER))
                .save(output);
    }
}
