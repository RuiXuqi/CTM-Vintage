package team.chisel.ctm.client.mixin;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.chisel.ctm.client.asm.CTMCoreMethods;

@Mixin(value = ForgeHooksClient.class, remap = false)
public class ForgeHooksClientMixin {
    @Inject(method = "getDamageModel", at = @At("HEAD"))
    private static void preDamageModel(CallbackInfoReturnable<IBakedModel> cir) {
        CTMCoreMethods.preDamageModel();
    }

    @Inject(method = "getDamageModel", at = @At("RETURN"))
    private static void postDamageModel(CallbackInfoReturnable<IBakedModel> cir) {
        CTMCoreMethods.postDamageModel();
    }
}
