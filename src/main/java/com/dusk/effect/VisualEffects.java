package com.dusk.effect;

import com.dusk.client.ClientDreadState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class VisualEffects {

    private static final RandomSource RNG = RandomSource.create();

    public static void register() {
        HudRenderCallback.EVENT.register(VisualEffects::onHudRender);
    }

    private static void onHudRender(GuiGraphics g, net.minecraft.client.DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        renderVignette(g, w, h);
        renderPhantomShadow(g, w, h);
        renderFlicker(g, w, h);
        renderFade(g, w, h);

        maybeSpawnFlicker(w, h);
        maybeSpawnShadow(w, h);
    }

    // True gradient vignette using fillGradient for top/bottom,
    // per-pixel strips for left/right — no hard edges
    private static void renderVignette(GuiGraphics g, int w, int h) {
        float base  = ClientDreadState.vignetteAlpha;
        float pulse = ClientDreadState.vignettePulse * 0.22f;
        float alpha = Math.min(1f, base + pulse);
        if (alpha < 0.01f) return;

        int maxA = (int)(alpha * 230);
        int dark = (maxA << 24);
        int clear = 0x00000000;

        int vigH = (int)(h * 0.52f);
        int vigW = (int)(w * 0.44f);

        // Top and bottom: true linear gradient
        g.fillGradient(0, 0, w, vigH,      dark,  clear);
        g.fillGradient(0, h - vigH, w, h,  clear, dark);

        // Left: per-pixel vertical strips, cubic falloff
        for (int x = 0; x < vigW; x += 2) {
            float t = 1f - (float) x / vigW;
            int a = (int)(alpha * t * t * t * 210);
            if (a < 2) break;
            g.fill(x, vigH, x + 2, h - vigH, (a << 24));
        }
        // Right: mirrored
        for (int x = 0; x < vigW; x += 2) {
            float t = 1f - (float) x / vigW;
            int a = (int)(alpha * t * t * t * 210);
            if (a < 2) break;
            g.fill(w - x - 2, vigH, w - x, h - vigH, (a << 24));
        }
    }

    // Phantom shadow: brief dark humanoid silhouette at mid-screen
    // Very faint — player sees something but it's already gone when they look directly
    private static void renderPhantomShadow(GuiGraphics g, int w, int h) {
        float sa = ClientDreadState.shadowAlpha;
        if (sa < 0.01f) return;

        int x  = ClientDreadState.shadowX;
        int y  = ClientDreadState.shadowY;
        int sw = ClientDreadState.shadowW;
        int sh = ClientDreadState.shadowH;
        int a  = (int)(sa * 50);
        int color = (a << 24);

        g.fill(x,        y,        x + sw,      y + sh, color);          // body
        g.fill(x + sw/4, y - sw/2, x + sw*3/4,  y,      color);          // head
    }

    // Peripheral flicker: tiny bright shimmer at screen corners
    private static void renderFlicker(GuiGraphics g, int w, int h) {
        float fa = ClientDreadState.flickerAlpha;
        if (fa < 0.05f) return;
        int x = ClientDreadState.flickerX;
        int y = ClientDreadState.flickerY;
        int a = (int)(fa * 60);
        g.fill(x, y, x + 4, y + 4, (a << 24) | 0x00CCCCCC);
    }

    private static void renderFade(GuiGraphics g, int w, int h) {
        float fa = ClientDreadState.fadeAlpha;
        if (fa < 0.01f) return;
        g.fill(0, 0, w, h, ((int)(fa * 255) << 24));
    }

    // Spawn flicker at random screen corner when score in range
    private static void maybeSpawnFlicker(int w, int h) {
        float s = ClientDreadState.score;
        if (s < ClientDreadState.FLICKER_START) return;
        if (ClientDreadState.flickerCooldown > 0) return;
        if (ClientDreadState.flickerAlpha > 0f) return;

        float intensity = (s - ClientDreadState.FLICKER_START) / (0.90f - ClientDreadState.FLICKER_START);
        intensity = Math.min(1f, intensity);
        if (RNG.nextFloat() > 0.003f + intensity * 0.020f) return;

        int corner = RNG.nextInt(4);
        int margin = 15 + RNG.nextInt(30);
        switch (corner) {
            case 0 -> { ClientDreadState.flickerX = RNG.nextInt(margin);           ClientDreadState.flickerY = RNG.nextInt(margin); }
            case 1 -> { ClientDreadState.flickerX = w - margin + RNG.nextInt(margin); ClientDreadState.flickerY = RNG.nextInt(margin); }
            case 2 -> { ClientDreadState.flickerX = RNG.nextInt(margin);           ClientDreadState.flickerY = h - margin + RNG.nextInt(margin); }
            default -> { ClientDreadState.flickerX = w - margin + RNG.nextInt(margin); ClientDreadState.flickerY = h - margin + RNG.nextInt(margin); }
        }
        ClientDreadState.flickerAlpha   = 0.5f + RNG.nextFloat() * 0.5f;
        ClientDreadState.flickerCooldown = (int)(65 - intensity * 45) + RNG.nextInt(40);
    }

    // Spawn phantom shadow at mid-distance (not corner, not center)
    private static void maybeSpawnShadow(int w, int h) {
        float s = ClientDreadState.score;
        if (s < ClientDreadState.SHADOW_START) return;
        if (ClientDreadState.shadowCooldown > 0) return;
        if (ClientDreadState.shadowAlpha > 0.05f) return;

        float intensity = (s - ClientDreadState.SHADOW_START) / (1f - ClientDreadState.SHADOW_START);
        intensity = Math.min(1f, intensity);
        if (RNG.nextFloat() > 0.0008f + intensity * 0.006f) return;

        // Place at screen mid-zone (20-80% from edges), tall and narrow
        int marginX = (int)(w * 0.20f);
        int marginY = (int)(h * 0.15f);
        int sw = 10 + RNG.nextInt(8);
        int sh = sw * 4;

        ClientDreadState.shadowX = marginX + RNG.nextInt(w - marginX * 2 - sw);
        ClientDreadState.shadowY = marginY + RNG.nextInt(h - marginY * 2 - sh);
        ClientDreadState.shadowW = sw;
        ClientDreadState.shadowH = sh;
        ClientDreadState.shadowAlpha   = 0.4f + RNG.nextFloat() * 0.5f;
        // Long cooldown — this should be rare and shocking, not frequent
        ClientDreadState.shadowCooldown = 300 + RNG.nextInt(400);
    }
}
