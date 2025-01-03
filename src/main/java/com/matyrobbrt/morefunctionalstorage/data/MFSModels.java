package com.matyrobbrt.morefunctionalstorage.data;

import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MFSModels extends ItemModelProvider {
    public MFSModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MoreFunctionalStorage.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(MoreFunctionalStorage.PLACER_UPGRADE.asItem());
        basicItem(MoreFunctionalStorage.BREAKER_UPGRADE.asItem());
        basicItem(MoreFunctionalStorage.REFILL_UPGRADE.asItem());
        basicItem(MoreFunctionalStorage.DIMENSIONAL_REFILL_UPGRADE.asItem());
        basicItem(MoreFunctionalStorage.STONECUTTING_UPGRADE.asItem());

        basicItem(MoreFunctionalStorage.SPEED_UPGRADE_AUGMENT.asItem());
    }
}
