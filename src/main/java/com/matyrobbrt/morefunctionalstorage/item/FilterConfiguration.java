package com.matyrobbrt.morefunctionalstorage.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public record FilterConfiguration(
        NonNullList<FilterStack> filters,
        boolean blacklist,
        boolean matchComponents,
        boolean matchTags
) implements Predicate<ItemStack> {
    public static final FilterConfiguration EMPTY = new FilterConfiguration(NonNullList.withSize(9, FilterStack.EMPTY), false, false, false);
    public static final Codec<FilterConfiguration> CODEC = RecordCodecBuilder.create(in -> in.group(
            NonNullList.codecOf(FilterStack.CODEC).fieldOf("filters").forGetter(FilterConfiguration::filters),
            Codec.BOOL.optionalFieldOf("blacklist", false).forGetter(FilterConfiguration::blacklist),
            Codec.BOOL.optionalFieldOf("match_components", false).forGetter(FilterConfiguration::matchComponents),
            Codec.BOOL.optionalFieldOf("match_tags", false).forGetter(FilterConfiguration::matchTags)
    ).apply(in, FilterConfiguration::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FilterConfiguration> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(NonNullList::createWithCapacity, FilterStack.STREAM_CODEC, 9), FilterConfiguration::filters,
            ByteBufCodecs.BOOL, FilterConfiguration::blacklist,
            ByteBufCodecs.BOOL, FilterConfiguration::matchComponents,
            ByteBufCodecs.BOOL, FilterConfiguration::matchTags,
            FilterConfiguration::new
    );

    public FilterConfiguration withItemInSlot(int slot, ItemStack stack, @Nullable TagKey<Item> tag) {
        var newFilters = new ArrayList<FilterStack>(9);
        for (int i = 0; i < filters.size(); i++) {
            if (i == slot) {
                newFilters.add(new FilterStack(stack, Optional.ofNullable(tag)));
            } else {
                newFilters.add(filters.get(i));
            }
        }
        return new FilterConfiguration(NonNullList.copyOf(newFilters), this.blacklist, this.matchComponents, this.matchTags);
    }
    public FilterConfiguration withBlacklist(boolean blacklist) {
        return new FilterConfiguration(this.filters, blacklist, this.matchComponents, this.matchTags);
    }
    public FilterConfiguration withMatchComponents(boolean matchComponents) {
        return new FilterConfiguration(this.filters, this.blacklist, matchComponents, this.matchTags);
    }
    public FilterConfiguration withMatchTags(boolean matchTags) {
        return new FilterConfiguration(this.filters, this.blacklist, this.matchComponents, matchTags);
    }

    @Override
    public boolean test(ItemStack stack) {
        return blacklist != testDirectly(stack);
    }

    private boolean testDirectly(ItemStack stack) {
        for (var fstack : filters) {
            var filter = fstack.stack();
            if (filter.isEmpty()) continue;

            boolean isMatch = testTypeMatch(fstack, stack);
            if (isMatch) {
                if (matchComponents) {
                    if (Objects.equals(stack.getComponents(), filter.getComponents())) {
                        return true;
                    } else {
                        continue;
                    }
                }
                return true;
            }
        }

        return false;
    }

    private boolean testTypeMatch(FilterStack first, ItemStack second) {
        if (matchTags) {
            if (first.tag().isPresent()) {
                return second.is(first.tag().get());
            }
            return first.stack().getItemHolder().tags()
                    .anyMatch(second::is);
        }
        return ItemStack.isSameItem(first.stack(), second);
    }

    public record FilterStack(ItemStack stack, Optional<TagKey<Item>> tag) {
        public static final Codec<FilterStack> CODEC = RecordCodecBuilder.create(in -> in.group(
                ItemStack.OPTIONAL_CODEC.fieldOf("stack").forGetter(FilterStack::stack),
                TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(FilterStack::tag)
        ).apply(in, FilterStack::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, FilterStack> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, FilterStack::stack,
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC.map(ItemTags::create, TagKey::location)), FilterStack::tag,
                FilterStack::new
        );

        public static final FilterStack EMPTY = new FilterStack(ItemStack.EMPTY, Optional.empty());

        public boolean isEmpty() {
            return stack.isEmpty();
        }
    }
}
