package com.dusk.effect;

import com.dusk.client.ClientDreadState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

@Environment(EnvType.CLIENT)
public class VisualEffects {

    public static void register() {
        HudRenderCallback.EVENT.register(VisualEffects::onHudRender);
    }

    private static void onHudRender(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        renderVignette(graphics, w, h);
        renderFade(graphics, w, h);
    }

    private static void renderVignette(GuiGraphics graphics, int w, int h) {
        float alpha = ClientDreadState.vignetteAlpha;
        if (alpha <= 0) return;

        int a = (int)(alpha * 255);
        int color = (a << 24);

        // Draw darkened border (simple gradient approximation)
        int borderW = w / 5;
        int borderH = h / 5;

        // top
        graphics.fill(0, 0, w, borderH, color);
        // bottom
        graphics.fill(0, h - borderH, w, h, color);
        // left
        graphics.fill(0, borderH, borderW, h - borderH, color);
        // right
        graphics.fill(w - borderW, borderH, w, h - borderH, color);
    }

    private static void renderFade(GuiGraphics graphics, int w, int h) {
        float alpha = ClientDreadState.fadeAlpha;
        if (alpha <= 0) return;

        // Smooth fade tick down after reaching max
        if (alpha > 0 && ClientDreadState.nyctophobiaStage < 6) {
            ClientDreadState.fadeAlpha = Math.max(0, alpha - 0.02f);
        }

        int a = (int)(alpha * 255);
        graphics.fill(0, 0, w, h, (a << 24));
    }
}
