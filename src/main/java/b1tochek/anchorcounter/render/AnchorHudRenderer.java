package b1tochek.anchorcounter.render;

import b1tochek.anchorcounter.AnchorCounterMod;
import b1tochek.anchorcounter.AnchorTracker;
import b1tochek.anchorcounter.config.AnchorConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;

import java.util.List;

public class AnchorHudRenderer {

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            render(drawContext);
        });
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        AnchorConfig config = AnchorConfig.get();
        if (!config.enabled) return;

        if (client.currentScreen != null) return;

        TextRenderer textRenderer = client.textRenderer;

        int x = config.hudX;
        int y = config.hudY;

        boolean hasAnyData = false;

        if (config.showSelf && AnchorCounterMod.tracker.hasData(client.player.getUuid())) {
            hasAnyData = true;
        }

        if (config.showOthers) {
            for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                if (player == client.player) continue;
                if (AnchorCounterMod.tracker.hasData(player.getUuid())) {
                    hasAnyData = true;
                    break;
                }
            }
        }

        if (!hasAnyData) return;

        String header = "§d" + config.anchorSymbol + " AnchorCounter";
        context.drawTextWithShadow(textRenderer, header, x, y, 0xFFFFFF);
        y += 12;

        if (config.showSelf) {
            AnchorTracker.AnchorData selfData = AnchorCounterMod.tracker.getData(client.player.getUuid());
            if (selfData.hasData()) {
                String selfText = "§aYou: §f" + config.formatDisplay(selfData.placed, selfData.exploded);
                context.drawTextWithShadow(textRenderer, selfText, x, y, 0xFFFFFF);
                y += 11;
            }
        }

        if (config.showOthers) {
            List<AbstractClientPlayerEntity> players = client.world.getPlayers();

            for (AbstractClientPlayerEntity player : players) {
                if (player == client.player) continue;

                double dist = client.player.squaredDistanceTo(player);
                if (dist > config.maxRenderDistance * config.maxRenderDistance) continue;

                if (!AnchorCounterMod.tracker.hasData(player.getUuid())) continue;

                AnchorTracker.AnchorData data = AnchorCounterMod.tracker.getData(player.getUuid());
                String text = "§b" + player.getName().getString() + ": §f" + config.formatDisplay(data.placed, data.exploded);

                context.drawTextWithShadow(textRenderer, text, x, y, 0xFFFFFF);
                y += 11;
            }
        }
    }
}