package team.chisel.ctm.client.texture.type;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.render.TextureNormal;

/**
 * Normal Block Render Type
 */
public class TextureTypeNormal implements ITextureType {

    @TextureType("normal")
    public static final TextureTypeNormal INSTANCE = new TextureTypeNormal();

    @NotNull
    private static final ITextureContext EMPTY_CONTEXT = () -> 0L;

    @Override
    public ICTMTexture<TextureTypeNormal> makeTexture(@NotNull TextureInfo info) {
        return new TextureNormal(this, info);
    }

    @Override
    public ITextureContext getBlockRenderContext(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull ICTMTexture<?> tex) {
        return EMPTY_CONTEXT;
    }

    @Override
    public ITextureContext getContextFromData(long data) {
        return EMPTY_CONTEXT;
    }
}
