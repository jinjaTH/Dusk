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
        renderFlicker(g, w, h);
        renderFade(g, w, h);
        maybeSpawnFlicker(w, h);
    }

    // Soft vignette: multiple concentric layers with quadratic falloff
    // Creates a smooth "eye narrows in darkness" effect
    private static void renderVignette(GuiGraphics g, int w, int h) {
        float alpha = ClientDreadState.vignetteAlpha;
        if (alpha < 0.01f) return;

        int layers = 22;
        float vigW = w * 0.48f;
        float vigH = h * 0.48f;

        for (int i = 0; i < layers; i++) {
            float t = 1f - (float) i / layers;          // 1.0 at edge → 0 inside
            float layerA = alpha * t * t * t;            // cubic — very smooth
            int a = (int) (layerA * 210);
            if (a < 2) continue;
            int color = (a << 24);

            float progress = (float) i / layers;
            int ox = (int) (progress * vigW);
            int oy = (int) (progress * vigH);
            int nx = (int) ((progress + 1f / layers) * vigW);
            int ny = (int) ((progress + 1f / layers) * vigH);

            // top band
            g.fill(ox, oy, w - ox, oy + Math.max(1, ny - oy), color);
            // bottom band
            g.fill(ox, h - oy - Math.max(1, ny - oy), w - ox, h - oy, color);
            // left band (between top/bottom bands)
            g.fill(ox, oy + Math.max(1, ny - oy),
                   ox + Math.max(1, nx - ox), h - oy - Math.max(1, ny - oy), color);
            // right band
            g.fill(w - ox - Math.max(1, nx - ox), oy + Math.max(1, ny - oy),
                   w - ox, h - oy - Math.max(1, ny - oy), color);
        }
    }

    // Peripheral flicker: brief shimmer at screen corners, gone in ~3 frames
    // Simulates peripheral vision hallucination
    private static void renderFlicker(GuiGraphics g, int w, int h) {
        float fa = ClientDreadState.flickerAlpha;
        if (fa < 0.05f) return;

        int x = ClientDreadState.flickerX;
        int y = ClientDreadState.flickerY;
        int size = 5 + RNG.nextInt(4);
        // Very faint white/grey shimmer
        int a = (int) (fa * 55);
        g.fill(x, y, x + size, y + size, (a << 24) | 0x00CCCCCC);
    }

    // Called each frame to maybe trigger a new flicker
    private static void maybeSpawnFlicker(int w, int h) {
        float s = ClientDreadState.score;
        if (s < ClientDreadState.FLICKER_START) return;
        if (ClientDreadState.flickerCooldown > 0) return;

        // Probability scales with score: rare at 0.62, frequent at 0.90
        float intensity = (s - ClientDreadState.FLICKER_START) / (0.90f - ClientDreadState.FLICKER_START);
        intensity = Math.min(1f, intensity);
        // Chance per frame: 0.3% → 2.5% as intensity grows
        float chance = 0.003f + intensity * 0.022f;
        if (RNG.nextFloat() > chance) return;

        // Pick a random screen corner region
        int corner = RNG.nextInt(4);
        int margin = 15 + RNG.nextInt(25);
        switch (corner) {
            case 0 -> { ClientDreadState.flickerX = RNG.nextInt(margin);
                        ClientDreadState.flickerY = RNG.nextInt(margin); }
            case 1 -> { ClientDreadState.flickerX = w - margin + RNG.nextInt(margin);
                        ClientDreadState.flickerY = RNG.nextInt(margin); }
            case 2 -> { ClientDreadState.flickerX = RNG.nextInt(margin);
                        ClientDreadState.flickerY = h - margin + RNG.nextInt(margin); }
            default -> { ClientDreadState.flickerX = w - margin + RNG.nextInt(margin);
                         ClientDreadState.flickerY = h - margin + RNG.nextInt(margin); }
        }
        ClientDreadState.flickerAlpha = 0.6f + RNG.nextFloat() * 0.4f;
        // Cooldown: shorter at higher scores
        ClientDreadState.flickerCooldown = (int) (60 - intensity * 45) + RNG.nextInt(40);
    }

    // Full-screen fade to black — slow buildup at max dread, instant clear
    private static void renderFade(GuiGraphics g, int w, int h) {
        float fa = ClientDreadState.fadeAlpha;
        if (fa < 0.01f) return;
        int a = (int) (fa * 255);
        g.fill(0, 0, w, h, (a << 24));
    }
}
