package team.chisel.ctm.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin {
    @Shadow
    protected abstract int interpolateColor(double p_188535_1_, int p_188535_3_, int p_188535_4_);

    @ModifyConstant(
            method = "updateAnimationInterpolated",
            constant = @Constant(intValue = -16777216)
    )
    private int clearOriginalAlphaMask(int originalMask) {
        return 0;
    }

    @ModifyExpressionValue(
            method = "updateAnimationInterpolated",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;interpolateColor(DII)I",
                    ordinal = 2
            )
    )
    private int interpolateAlpha(int j2, @Local(ordinal = 0) double d0, @Local(ordinal = 5) int j1, @Local(ordinal = 6) int k1) {
        int a1 = (j1 >> 24) & 0xFF;
        int a2 = (k1 >> 24) & 0xFF;
        return j2 | (this.interpolateColor(d0, a1, a2) << 24);
    }
}
