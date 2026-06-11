package com.hjsmc.deadeye.client;

import com.hjsmc.deadeye.DeadeyeClientConfig;
import com.hjsmc.deadeye.DeadeyeMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Matrix4f;

/**
 * Deadeye HUD: a screen-edge vignette (four gradient bands fading from the
 * configured color at the border to transparent toward the center, overlapping
 * in the corners) plus an eye icon in the upper part of the screen, like the
 * TACZ: Plus indicator. The eye texture is white-on-transparent and gets
 * tinted with the vignette color at draw time, so both effects always match.
 * Color, opacity, band width and the eye style come from {@link DeadeyeClientConfig}.
 */
public final class DeadeyeVignetteOverlay implements IGuiOverlay {
    private static final ResourceLocation EYE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MODID, "textures/gui/deadeye_eye.png");
    private static final int EYE_TEXTURE_SIZE = 128;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        float alpha = DeadeyeClientState.updateVignetteAlpha();
        float progress = DeadeyeClientState.fadeProgress();
        if (progress < 0.004F) {
            return;
        }

        int rgb = DeadeyeClientConfig.colorRgb();
        float r = (rgb >> 16 & 0xFF) / 255.0F;
        float g = (rgb >> 8 & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        if (alpha >= 0.004F) {
            renderVignette(guiGraphics, screenWidth, screenHeight, r, g, b, alpha);
        }
        if (DeadeyeClientConfig.eyeEnabled()) {
            renderEye(guiGraphics, screenWidth, screenHeight, r, g, b, progress);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void renderVignette(GuiGraphics guiGraphics, int screenWidth, int screenHeight,
                                       float r, float g, float b, float alpha) {
        float band = DeadeyeClientConfig.bandFraction();
        int bandX = Math.max(8, (int) (screenWidth * band));
        int bandY = Math.max(8, (int) (screenHeight * band));

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // top: colored at y=0, transparent at y=bandY
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, 0, bandY, 0).color(r, g, b, 0.0F).endVertex();
        buffer.vertex(matrix, screenWidth, bandY, 0).color(r, g, b, 0.0F).endVertex();
        buffer.vertex(matrix, screenWidth, 0, 0).color(r, g, b, alpha).endVertex();
        // bottom: transparent at y=h-bandY, colored at y=h
        buffer.vertex(matrix, 0, screenHeight - bandY, 0).color(r, g, b, 0.0F).endVertex();
        buffer.vertex(matrix, 0, screenHeight, 0).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, screenWidth, screenHeight, 0).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, screenWidth, screenHeight - bandY, 0).color(r, g, b, 0.0F).endVertex();
        // left: colored at x=0, transparent at x=bandX
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, 0, screenHeight, 0).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, bandX, screenHeight, 0).color(r, g, b, 0.0F).endVertex();
        buffer.vertex(matrix, bandX, 0, 0).color(r, g, b, 0.0F).endVertex();
        // right: transparent at x=w-bandX, colored at x=w
        buffer.vertex(matrix, screenWidth - bandX, 0, 0).color(r, g, b, 0.0F).endVertex();
        buffer.vertex(matrix, screenWidth - bandX, screenHeight, 0).color(r, g, b, 0.0F).endVertex();
        buffer.vertex(matrix, screenWidth, screenHeight, 0).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, screenWidth, 0, 0).color(r, g, b, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void renderEye(GuiGraphics guiGraphics, int screenWidth, int screenHeight,
                                  float r, float g, float b, float progress) {
        int size = DeadeyeClientConfig.eyeSize();
        int x = (screenWidth - size) / 2;
        int y = (int) (screenHeight * DeadeyeClientConfig.eyeVerticalOffset());
        RenderSystem.setShaderColor(r, g, b, progress * 0.9F);
        guiGraphics.blit(EYE_TEXTURE, x, y, size, size, 0.0F, 0.0F,
                EYE_TEXTURE_SIZE, EYE_TEXTURE_SIZE, EYE_TEXTURE_SIZE, EYE_TEXTURE_SIZE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
