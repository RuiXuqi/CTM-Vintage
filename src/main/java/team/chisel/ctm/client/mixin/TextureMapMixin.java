package team.chisel.ctm.client.mixin;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.chisel.ctm.client.asm.CTMCoreMethods;

@Mixin(TextureMap.class)
public class TextureMapMixin {
    @Inject(method = "registerSprite(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", at = @At("RETURN"))
    private void onSpriteRegister(ResourceLocation location, @NotNull CallbackInfoReturnable<TextureAtlasSprite> cir) {
        CTMCoreMethods.onSpriteRegister((TextureMap) (Object) this, cir.getReturnValue());
    }
}
