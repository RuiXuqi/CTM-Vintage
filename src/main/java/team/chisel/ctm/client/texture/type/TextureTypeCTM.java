package team.chisel.ctm.client.texture.type;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.Configurations;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextCTM;
import team.chisel.ctm.client.texture.render.TextureCTM;

@TextureType("ctm")
public class TextureTypeCTM implements ITextureType {

    @SuppressWarnings("unchecked")
    @Override
    public ICTMTexture<? extends TextureTypeCTM> makeTexture(@NotNull TextureInfo info) {
        return new TextureCTM<>(this, info);
    }

    @Override
    public TextureContextCTM getBlockRenderContext(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull ICTMTexture<?> tex) {
        return new TextureContextCTM(state, world, pos, (TextureCTM<?>) tex);
    }

    @Override
    public int getQuadsPerSide() {
        return Configurations.disableCTM ? 1 : 4;
    }

    @Override
    public int requiredTextures() {
        return 2;
    }

    @Override
    public ITextureContext getContextFromData(long data) {
        throw new UnsupportedOperationException();
    }
}
