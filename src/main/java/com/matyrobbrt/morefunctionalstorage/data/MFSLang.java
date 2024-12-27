package com.matyrobbrt.morefunctionalstorage.data;

import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.util.Texts;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MFSLang extends LanguageProvider {
    public MFSLang(PackOutput output) {
        super(output, MoreFunctionalStorage.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addItem(MoreFunctionalStorage.PLACER_UPGRADE, "Placer Upgrade");
        addItem(MoreFunctionalStorage.BREAKER_UPGRADE, "Breaker Upgrade");
        addItem(MoreFunctionalStorage.REFILL_UPGRADE, "Refill Upgrade");
        addItem(MoreFunctionalStorage.DIMENSIONAL_REFILL_UPGRADE, "Dimensional Refill Upgrade");
        addItem(MoreFunctionalStorage.SPEED_UPGRADE_AUGMENT, "Speed Upgrade Augment");

        for (Texts value : Texts.values()) {
            add(value.key, value.translated);
        }
    }
}
