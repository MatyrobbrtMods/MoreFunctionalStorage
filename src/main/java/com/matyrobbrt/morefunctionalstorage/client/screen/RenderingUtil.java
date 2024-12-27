package com.matyrobbrt.morefunctionalstorage.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RenderingUtil {
    public static void renderItem(
            GuiGraphics graphics, ItemStack stack, int x, int y, int seed, int guiOffset,
            float redTint, float greenTint, float blueTint, float alphaTint
    ) {
        if (!stack.isEmpty()) {
            // Our tinting doesn't support foil since uses a combined vertex consumer
            if (stack.hasFoil()) {
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            var bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, seed);
            graphics.pose().pushPose();
            graphics.pose().translate((float) (x + 8), (float) (y + 8), (float) (150 + (bakedmodel.isGui3d() ? guiOffset : 0)));

            try {
                graphics.pose().scale(16.0F, -16.0F, 16.0F);
                RenderSystem.applyModelViewMatrix();

                boolean flag = !bakedmodel.usesBlockLight();
                if (flag) {
                    Lighting.setupForFlatItems();
                }

                Minecraft.getInstance()
                        .getItemRenderer()
                        .render(stack, ItemDisplayContext.GUI, false, graphics.pose(), renderType -> {
                            return new TintedVertexConsumer(
                                    graphics.bufferSource().getBuffer(renderType), redTint, greenTint, blueTint, alphaTint
                            );
                        }, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedmodel);
                graphics.flush();
                RenderSystem.enableDepthTest();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashreport);
            }

            graphics.pose().popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public static final class TintedVertexConsumer implements VertexConsumer {
        private final VertexConsumer wrapped;

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return wrapped.addVertex(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return wrapped.setColor((int)(red * this.red), (int)(green * this.green), (int)(blue * this.blue), (int)(alpha * this.alpha));
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return wrapped.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return wrapped.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return wrapped.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return wrapped.setNormal(normalX, normalY, normalZ);
        }

        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;

        public TintedVertexConsumer(VertexConsumer wrapped, float red, float green, float blue, float alpha) {
            this.wrapped = wrapped;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }
}
