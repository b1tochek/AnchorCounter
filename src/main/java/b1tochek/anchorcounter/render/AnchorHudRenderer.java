package b1tochek.anchorcounter.render;

import b1tochek.anchorcounter.AnchorCounterMod;
import b1tochek.anchorcounter.AnchorTracker;
import b1tochek.anchorcounter.config.AnchorConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;

public class AnchorHudRenderer {

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            render(drawContext);
        });
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        AnchorConfig config = AnchorConfig.get();
        if (!config.enabled) return;

        if (client.currentScreen != null) return;
    }
}