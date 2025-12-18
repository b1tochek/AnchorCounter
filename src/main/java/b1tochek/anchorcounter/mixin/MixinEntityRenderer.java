package b1tochek.anchorcounter.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {

    @Unique
    private T anchorCounter$capturedEntity;


    @Shadow
    protected abstract void renderLabelIfPresent(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Inject(method = "updateRenderState", at = @At("HEAD"))
    private void anchorCounter$captureEntity(T entity, S state, float tickDelta, CallbackInfo ci) {
        this.anchorCounter$capturedEntity = entity;
    }


    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
            ),
            index = 1
    )
    private Text anchorCounter$modifyLabelText(Text text) {
        if (text != null && anchorCounter$capturedEntity instanceof PlayerEntity player) {
            return anchorCounter$addAnchorCount(player, text);
        }
        return text;
    }

    @Unique
    private Text anchorCounter$addAnchorCount(PlayerEntity player, Text text) {
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