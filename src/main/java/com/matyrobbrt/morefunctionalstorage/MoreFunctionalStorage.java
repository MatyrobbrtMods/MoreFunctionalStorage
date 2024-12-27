package com.matyrobbrt.morefunctionalstorage;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.hrznstudio.titanium.block.redstone.RedstoneAction;
import com.hrznstudio.titanium.network.locator.PlayerInventoryFinder;
import com.matyrobbrt.morefunctionalstorage.attach.UpgradeDataManager;
import com.matyrobbrt.morefunctionalstorage.data.MFSLang;
import com.matyrobbrt.morefunctionalstorage.data.MFSModels;
import com.matyrobbrt.morefunctionalstorage.data.MFSRecipe;
import com.matyrobbrt.morefunctionalstorage.item.BreakerUpgrade;
import com.matyrobbrt.morefunctionalstorage.item.FilterConfiguration;
import com.matyrobbrt.morefunctionalstorage.item.MFSUpgrade;
import com.matyrobbrt.morefunctionalstorage.item.PlacerUpgrade;
import com.matyrobbrt.morefunctionalstorage.menu.BaseUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.menu.BreakerUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.menu.PlacerUpgradeMenu;
import com.matyrobbrt.morefunctionalstorage.packet.OpenDrawerMenuPayload;
import com.matyrobbrt.morefunctionalstorage.packet.OpenUpgradeMenuPayload;
import com.matyrobbrt.morefunctionalstorage.packet.ReplaceUpgradePayload;
import com.matyrobbrt.morefunctionalstorage.util.RelativeDirection;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Mod(MoreFunctionalStorage.MOD_ID)
public class MoreFunctionalStorage {
    public static final String MOD_ID = "morefunctionalstorage";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<UpgradeDataManager.Drawer>> UPGRADE_DATA_MANAGER = ATTACHMENTS.register("upgrade_data",
            () -> AttachmentType.builder(holder -> new UpgradeDataManager.Drawer((ControllableDrawerTile<?>) holder))
                    .serialize(new IAttachmentSerializer<>() {
                        @Override
                        public UpgradeDataManager.Drawer read(IAttachmentHolder holder, Tag tag, HolderLookup.Provider provider) {
                            var data = new UpgradeDataManager.Drawer((ControllableDrawerTile<?>) holder);
                            data.deserializeNBT(provider, (CompoundTag) tag);
                            return data;
                        }

                        @Override
                        public @Nullable Tag write(UpgradeDataManager.Drawer attachment, HolderLookup.Provider provider) {
                            return attachment.serializeNBT(provider);
                        }
                    }).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RedstoneAction>> REDSTONE_ACTION = COMPONENTS.registerComponentType("redstone_action", b -> b
            .persistent(Codec.STRING.xmap(RedstoneAction::valueOf, RedstoneAction::name))
            .networkSynchronized(ByteBufCodecs.VAR_INT.map(i -> RedstoneAction.values()[i], RedstoneAction::ordinal)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> AUGMENTS = COMPONENTS.registerComponentType("augments", b -> b
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> TOOL = COMPONENTS.registerComponentType("tool", b -> b
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RelativeDirection>> RELATIVE_DIRECTION = COMPONENTS.registerComponentType("relative_direction", b -> b
            .persistent(StringRepresentable.fromValues(RelativeDirection::values))
            .networkSynchronized(ByteBufCodecs.idMapper(i -> RelativeDirection.values()[i], RelativeDirection::ordinal)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GameProfile>> OWNER = COMPONENTS.registerComponentType("owner", b -> b
            .persistent(ExtraCodecs.GAME_PROFILE)
            .networkSynchronized(StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, GameProfile::getId,
                    ByteBufCodecs.STRING_UTF8, GameProfile::getName,
                    GameProfile::new
            )));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Integer>>> SELECTED_SLOTS = COMPONENTS.registerComponentType("selected_slots", b -> b
            .persistent(Codec.INT.sizeLimitedListOf(4))
            .networkSynchronized(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(4))));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FilterConfiguration>> FILTER = COMPONENTS.registerComponentType("filter", b -> b
            .persistent(FilterConfiguration.CODEC)
            .networkSynchronized(FilterConfiguration.STREAM_CODEC));

    public static final DeferredHolder<MenuType<?>, MenuType<PlacerUpgradeMenu>> PLACER_MENU = MENUS.register("placer_upgrade", () -> createMenu(PlacerUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<BreakerUpgradeMenu>> BREAKER_MENU = MENUS.register("breaker_upgrade", () -> createMenu(BreakerUpgradeMenu::new));
    public static final DeferredItem<PlacerUpgrade> PLACER_UPGRADE = ITEMS.register("placer_upgrade", () -> new PlacerUpgrade(new Item.Properties()));
    public static final DeferredItem<BreakerUpgrade> BREAKER_UPGRADE = ITEMS.register("breaker_upgrade", () -> new BreakerUpgrade(new Item.Properties()));

    public static final DeferredItem<Item> SPEED_UPGRADE_AUGMENT = ITEMS.register("speed_upgrade_augment", () -> new Item(new Item.Properties()) {
        {
            FunctionalStorage.TAB.getTabList().add(this);
        }
    });

    public MoreFunctionalStorage(ModContainer container, IEventBus bus) {
        ITEMS.register(bus);
        MENUS.register(bus);
        ATTACHMENTS.register(bus);
        COMPONENTS.register(bus);

        container.registerConfig(ModConfig.Type.SERVER, MFSConfig.SPEC, MOD_ID + "-server.toml");

        bus.addListener((final RegisterPayloadHandlersEvent event) -> {
            event.registrar(container.getModInfo().getVersion().toString())
                    .playToServer(OpenDrawerMenuPayload.TYPE, OpenDrawerMenuPayload.CODEC, OpenDrawerMenuPayload::handle)
                    .playToServer(OpenUpgradeMenuPayload.TYPE, OpenUpgradeMenuPayload.CODEC, OpenUpgradeMenuPayload::handle)
                    .playToServer(ReplaceUpgradePayload.TYPE, ReplaceUpgradePayload.CODEC, ReplaceUpgradePayload::handle)
            ;
        });

        bus.addListener((final FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            PlayerInventoryFinder.FINDERS.put("mfs_drawer_upgrade", new PlayerInventoryFinder(
                    player -> ((BaseUpgradeMenu) player.containerMenu).drawer.getUtilityUpgrades().getSlots(),
                    (player, integer) -> ((BaseUpgradeMenu) player.containerMenu).drawer.getUtilityUpgrades().getStackInSlot(integer),
                    (player, i, itemStack) -> ((BaseUpgradeMenu) player.containerMenu).drawer.getUtilityUpgrades().setStackInSlot(i, itemStack)
            ));
        }));

        if (!FMLEnvironment.production) {
            bus.addListener(MoreFunctionalStorage::onDataGen);
        }
    }

    private static void onDataGen(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeClient(), new MFSModels(event.getGenerator().getPackOutput(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeClient(), new MFSLang(event.getGenerator().getPackOutput()));

        event.getGenerator().addProvider(event.includeServer(), new MFSRecipe(event.getGenerator().getPackOutput(), event.getLookupProvider()));
    }

    private static <T extends BaseUpgradeMenu> MenuType<T> createMenu(MFSUpgrade.MenuFactory factory) {
        return IMenuTypeExtension.create((windowId, inv, data) -> (T) factory.create(windowId, inv, data.readBlockPos(), data.readVarInt()));
    }
}
