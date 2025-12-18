package b1tochek.anchorcounter.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import b1tochek.anchorcounter.AnchorCounterMod;
import b1tochek.anchorcounter.AnchorTracker;
import b1tochek.anchorcounter.config.AnchorConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V"))
    public void anchorCounter$modifyLabel(EntityRenderer<Entity> instance, Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, Operation<Void> original) {
        if (entity.getWorld().isClient && text != null && entity instanceof PlayerEntity player) {
            text = addAnchorCount(player, text);
        }

        original.call(instance, entity, text, matrices, vertexConsumers, light, tickDelta);
    }

    private Text addAnchorCount(PlayerEntity player, Text text) {
        AnchorConfig config = AnchorConfig.get();

        if (!config.enabled) return text;

        if (!AnchorCounterMod.tracker.hasData(player.getUuid())) return text;

        AnchorTracker.AnchorData data = AnchorCounterMod.tracker.getData(player.getUuid());

        if (data.placed <= 0) return text;

        int color = AnchorConfig.parseColor(config.nametagColor);

        MutableText label = text.copy().append(" ");
        MutableText counter = Text.literal("-" + data.placed);
        counter.setStyle(Style.EMPTY.withColor(color));
        label.append(counter);

        return label;
    }
}