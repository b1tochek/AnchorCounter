package b1tochek.anchorcounter.render;

import b1tochek.anchorcounter.AnchorCounterMod;
import b1tochek.anchorcounter.AnchorTracker;
import b1tochek.anchorcounter.config.AnchorConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class AnchorNametagRenderer {

    public static void render(AbstractClientPlayerEntity player,
                              MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers) {

        AnchorConfig config = AnchorConfig.get();

        if (!config.enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        boolean isSelf = player == client.player;
        if (isSelf && !config.showSelf) return;
        if (!isSelf && !config.showOthers) return;

        double distance = client.player.squaredDistanceTo(player);
        if (distance > config.maxRenderDistance * config.maxRenderDistance) return;

        if (!AnchorCounterMod.tracker.hasData(player.getUuid())) return;

        AnchorTracker.AnchorData data = AnchorCounterMod.tracker.getData(player.getUuid());

        String text = config.formatDisplay(data.placed, data.exploded);

        renderNametag(player, text, matrices, vertexConsumers, config);
    }

    private static void renderNametag(AbstractClientPlayerEntity player,
                                      String text,
                                      MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers,
                                      AnchorConfig config) {

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        matrices.push();

        float yOffset = player.getHeight() + 0.5f + config.nametagOffsetY;
        matrices.translate(0.0, yOffset, 0.0);

        matrices.multiply(client.getEntityRenderDispatcher().getRotation());

        float scale = config.nametagScale;
        matrices.scale(-scale, -scale, scale);

        float x = -textRenderer.getWidth(text) / 2.0f;
        float y = 0;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        renderColoredText(textRenderer, text, x, y, matrix, vertexConsumers, config);

        matrices.pop();
    }

    private static void renderColoredText(TextRenderer textRenderer,
                                          String text,
                                          float x,
                                          float y,
                                          Matrix4f matrix,
                                          VertexConsumerProvider vertexConsumers,
                                          AnchorConfig config) {

        String[] parts = text.split(" ");
        float currentX = x;

        for (String part : parts) {
            int color = getColorForPart(part, config);

            int bgColor = 0x40000000;
            textRenderer.draw(
                    part,
                    currentX,
                    y,
                    color,
                    false,
                    matrix,
                    vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL,
                    bgColor,
                    15728880
            );

            currentX += textRenderer.getWidth(part + " ");
        }
    }

    private static int getColorForPart(String part, AnchorConfig config) {
        if (part.equals(config.anchorSymbol)) {
            return AnchorConfig.parseColor(config.symbolColor);
        } else if (part.equals(config.placedText)) {
            return AnchorConfig.parseColor(config.placedColor);
        } else if (part.equals(config.explodedText)) {
            return AnchorConfig.parseColor(config.explodedColor);
        } else if (part.equals("|")) {
            return AnchorConfig.parseColor("#AAAAAA");
        } else {
            try {
                Integer.parseInt(part);
                return AnchorConfig.parseColor(config.numberColor);
            } catch (NumberFormatException e) {
                return AnchorConfig.parseColor(config.textColor);
            }
        }
    }
}