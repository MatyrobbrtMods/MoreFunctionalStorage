package com.matyrobbrt.morefunctionalstorage.util;

import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.Collections;
import java.util.List;

public enum Texts implements Component {
    DIRECTION("tooltip", "direction", "Direction: %s (%s)"),
    DIRECTION_SIMPLE("tooltip", "direction_simple", "§eDirection§r: %s"),
    SLOT_SELECTED("tooltip", "slot_selected", "Slot is selected as upgrade %s"),
    LCLICK_DESELECT("tooltip", "lclick_deselect", "Left Click to deselect"),
    LCLICK_SELECT("tooltip", "lclick_select", "Left Click to select slot as upgrade %s"),
    UPGRADE_SPEED("tooltip", "upgrade_speed", "§eUpgrade speed§r: operates every %s ticks"),
    HAS_FILTERS_CONFIGURED("tooltip", "has_filters", "§oHas filters configured§r"),
    HOLD_SHIFT("tooltip", "hold_shift", "Hold §7SHIFT§r for information"),
    RCLICK_CONFIGURE("tooltip", "rclick_configure", "Right click to configure"),
    OPEN_FILTERS_PANEL("tooltip", "filters_panel.open", "Open filters panel"),
    CLOSE_FILTERS_PANEL("tooltip", "filters_panel.close", "Close filters panel"),
    TAG_SELECTION("tooltip", "tag_selection", "Scroll to choose the tag to match from the list below:"),
    ANY_OF_THE_ABOVE("tooltip", "tag.any_of_the_above", "any of the above"),
    TAG_MATCHING("tooltip", "tag_matching", "Tag matching is %s"),
    COMPONENT_MATCHING("tooltip", "component_matching", "Strict NBT matching is %s"),
    WHITELIST_BLACKLIST("tooltip", "whitelist_blacklist", "Current filter mode is %s"),
    TOOL("tooltip", "tool", "§eTool§r: §d%s§r"),
    NO_TOOL_INSTALLED("tooltip", "no_tool", "§cNo tool is installed! Install a tool for the upgrade to function§r"),
    REFILL_TARGET("tooltip", "refill_target", "Refill target: %s"),

    SOURCE("tooltip", "source", "source"),
    DESTINATION("tooltip", "target", "target"),
    SELECTED("tooltip", "selected", "selected"),
    WHITELIST("tooltip", "whitelist", "whitelist"),
    BLACKLIST("tooltip", "blacklist", "blacklist"),
    ENABLE("tooltip", "enable", "enable"),
    ENABLED("tooltip", "enabled", "enabled"),
    DISABLE("tooltip", "disable", "disable"),
    DISABLED("tooltip", "disabled", "disabled"),
    CLICK_TO("tooltip", "click_to", "Click to %s"),
    CLICK_TO_TOGGLE("tooltip", "click_to_toggle", "Click to toggle to %s"),

    PLACER_UPGRADE("tooltip.item", "placer_upgrade", "Places blocks from the drawer. Right click inside a drawer menu to configure"),
    REFILL_UPGRADE("tooltip.item", "refill_upgrade", "Refill items from the drawer into the player's hotbar, inventory or ender chest.\nRight click inside a drawer menu to configure"),
    BREAKER_UPGRADE("tooltip.item", "breaker_upgrade", "Breaks blocks and inserts them into the drawer. Right click inside a drawer menu to configure\nRequires a tool to be provided (durability will not be consumed)"),
    STONECUTTING_UPGRADE("tooltip.item", "stonecutting_upgrade", "Stonecuts items from the drawer.\nThe output can be selected either by locking the slots or using the filter."),

    REFILL_HOTBAR("tooltip.refill_target", "hotbar", "hotbar"),
    REFILL_MAIN_INV("tooltip.refill_target", "main_inv", "main inventory"),
    REFILL_ENDER_CHEST("tooltip.refill_target", "ender_chest", "ender chest");

    public final String key, translated;
    private final Component text;

    Texts(String type, String key, String translated) {
        this.key = type + "." + MoreFunctionalStorage.MOD_ID + "." + key;
        this.translated = translated;
        this.text = format();
    }

    public MutableComponent format(Object... args) {
        return Component.translatable(key, args);
    }

    public MutableComponent format(ChatFormatting colour, Object... args) {
        return Component.translatable(key, args).withStyle(colour);
    }

    @Override
    public Style getStyle() {
        return text.getStyle();
    }

    @Override
    public ComponentContents getContents() {
        return text.getContents();
    }

    @Override
    public List<Component> getSiblings() {
        return Collections.unmodifiableList(text.getSiblings());
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return text.getVisualOrderText();
    }
}
