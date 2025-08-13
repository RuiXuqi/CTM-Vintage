package team.chisel.ctm.client.texture.type;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextNewCTM;
import team.chisel.ctm.client.texture.render.TextureNewCTM;

@TextureType("optifine_fullctm")
public class TextureTypeOptifineFullctm implements ITextureType {

    @Override
    public ITextureContext getBlockRenderContext(IBlockState state, IBlockAccess world, BlockPos pos, ICTMTexture<?> tex) {
        return new TextureContextNewCTM(state, world, pos, tex);
    }

    @Override
    public ITextureContext getContextFromData(long data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ICTMTexture<? extends TextureTypeOptifineFullctm> makeTexture(TextureInfo info) {
        return new TextureNewCTM<>(this, info);
    }
}
