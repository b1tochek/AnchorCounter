package b1tochek.anchorcounter.mixin;

import b1tochek.anchorcounter.AnchorCounterMod;
import b1tochek.anchorcounter.AnchorTracker;
import b1tochek.anchorcounter.config.AnchorConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(method = "render*", at = @At("RETURN"))
    private void anchorCounter$onRender(AbstractClientPlayerEntity player,
                                        float yaw,
                                        float tickDelta,
                                        MatrixStack matrices,
                                        VertexConsumerProvider vertexConsumers,
                                        int light,
                                        CallbackInfo ci) {

        // Отладка
        System.out.println("[AnchorCounter] Render called for: " + player.getName().getString());

        AnchorConfig config = AnchorConfig.get();
        if (!config.enabled) {
            System.out.println("[AnchorCounter] Disabled");
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        boolean isSelf = player == client.player;
        if (isSelf && !config.showSelf) return;
        if (!isSelf && !config.showOthers) return;

        double distance = client.player.squaredDistanceTo(player);
        if (distance > config.maxRenderDistance * config.maxRenderDistance) return;

        boolean hasData = AnchorCounterMod.tracker.hasData(player.getUuid());
        System.out.println("[AnchorCounter] Has data for " + player.getName().getString() + ": " + hasData);

        if (!hasData) return;

        AnchorTracker.AnchorData data = AnchorCounterMod.tracker.getData(player.getUuid());
        String text = config.formatDisplay(data.placed, data.exploded);

        System.out.println("[AnchorCounter] Rendering: " + text);

        renderLabel(player, text, matrices, vertexConsumers, light, config);
    }

    private void renderLabel(AbstractClientPlayerEntity player,
                             String text,
                             MatrixStack matrices,
                             VertexConsumerProvider vertexConsumers,
                             int light,
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
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        textRenderer.draw(
                text,
                x,
                0,
                0xFFFFFF,
                false,
                matrix,
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0x40000000,
                light
        );

        matrices.pop();
    }
}