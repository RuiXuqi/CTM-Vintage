package team.chisel.ctm.client.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.chisel.ctm.client.asm.CTMCoreMethods;
import team.chisel.ctm.client.state.CTMExtendedState;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "getExtendedState", at = @At("RETURN"), cancellable = true, remap = false)
    private void wrapExtendedState(IBlockState state, IBlockAccess world, BlockPos pos, @NotNull CallbackInfoReturnable<IBlockState> cir) {
        cir.setReturnValue(new CTMExtendedState(cir.getReturnValue(), world, pos));
    }

    @Inject(method = "canRenderInLayer", at = @At("HEAD"), cancellable = true, remap = false)
    private void onCanRenderInLayer(IBlockState state, BlockRenderLayer layer, CallbackInfoReturnable<Boolean> cir) {
        Boolean result = CTMCoreMethods.canRenderInLayer(state, layer);
        if (result != null) cir.setReturnValue(result);
    }
}
