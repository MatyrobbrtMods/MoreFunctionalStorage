package com.matyrobbrt.morefunctionalstorage.client;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.client.screen.container.BasicAddonScreen;
import com.matyrobbrt.morefunctionalstorage.MoreFunctionalStorage;
import com.matyrobbrt.morefunctionalstorage.client.screen.BaseUpgradeScreen;
import com.matyrobbrt.morefunctionalstorage.client.screen.BreakerUpgradeScreen;
import com.matyrobbrt.morefunctionalstorage.client.screen.PlacerUpgradeScreen;
import com.matyrobbrt.morefunctionalstorage.client.screen.RefillUpgradeScreen;
import com.matyrobbrt.morefunctionalstorage.item.MFSUpgrade;
import com.matyrobbrt.morefunctionalstorage.mixin.SlotItemHandlerAccess;
import com.matyrobbrt.morefunctionalstorage.packet.OpenUpgradeMenuPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.stream.IntStream;

@Mod(value = MoreFunctionalStorage.MOD_ID, dist = Dist.CLIENT)
public class MFSClient {
    public MFSClient(ModContainer container, IEventBus bus) {
        NeoForge.EVENT_BUS.addListener((final ScreenEvent.MouseButtonPressed.Pre event) -> {
            var mc = Minecraft.getInstance();
            var screen = mc.screen;

            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (screen instanceof BasicAddonScreen bas && bas.getMenu().getProvider() instanceof ControllableDrawerTile<?>) {
                    if (bas.getSlotUnderMouse() instanceof SlotItemHandler sih && sih.getItem().getItem() instanceof MFSUpgrade) {
                        PacketDistributor.sendToServer(new OpenUpgradeMenuPayload(
                                ((SlotItemHandlerAccess) sih).morefuncstorage$getIndex()
                        ));
                        event.setCanceled(true);
                    }
                }
            } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && screen instanceof BaseUpgradeScreen<?> bcs && !bcs.getMenu().getCarried().isEmpty()) {
                for (IScreenAddon addon : bcs.getAddons()) {
                    if (addon instanceof FiltersPanelAddon ad) {
                        if (ad.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        });
        NeoForge.EVENT_BUS.addListener((final ScreenEvent.MouseButtonReleased.Pre event) -> {
            var mc = Minecraft.getInstance();
            var screen = mc.screen;

            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && screen instanceof BaseUpgradeScreen<?> bcs && !bcs.getMenu().getCarried().isEmpty()) {
                for (IScreenAddon addon : bcs.getAddons()) {
                    if (addon instanceof FiltersPanelAddon ad) {
                        if (ad.getSlotUnderMouse(event.getMouseX(), event.getMouseY()) >= 0) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        });

        bus.addListener(MFSClient::registerScreens);
    }

    private static void registerScreens(final RegisterMenuScreensEvent event) {
        event.register(MoreFunctionalStorage.PLACER_MENU.get(), PlacerUpgradeScreen::new);
        event.register(MoreFunctionalStorage.BREAKER_MENU.get(), BreakerUpgradeScreen::new);
        event.register(MoreFunctionalStorage.REFILL_MENU.get(), RefillUpgradeScreen::new);
    }

    public static boolean isInsideDrawerUI(ItemStack stack) {
        return Minecraft.getInstance().screen instanceof BasicAddonScreen bas && bas.getMenu().getProvider() instanceof ControllableDrawerTile<?> tile
                && IntStream.range(0, tile.getUtilitySlotAmount())
                    .anyMatch(p -> tile.getUtilityUpgrades().getStackInSlot(p) == stack);
    }

    public static boolean isShiftDown() {
        return Screen.hasShiftDown();
    }
}
