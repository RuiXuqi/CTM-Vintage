package team.chisel.ctm.client.newctm;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.util.TextureInfo;

import java.util.List;

public class TextureTypeCustom implements ITextureType {

    private final ICTMLogic logic;

    public TextureTypeCustom(ICTMLogic customLogic) {
        this.logic = customLogic;
    }

    @Override
    public ITextureContext getBlockRenderContext(IBlockState state, IBlockAccess world, BlockPos pos, ICTMTexture<?> tex) {
        return new TextureContextCustomCTM(state, world, pos, tex, logic);
    }

    @Override
    public ITextureContext getContextFromData(long data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ISubmap> getOutputFaces() {
        return logic.outputSubmaps();
    }

    @Override
    public int requiredTextures() {
        return logic.requiredTextures();
    }

    @Override
    public TextureCustomCTM<? extends TextureTypeCustom> makeTexture(TextureInfo info) {
        return new TextureCustomCTM<>(this, info);
    }

    public ISubmap getFallbackUvs() {
        return logic.getFallbackUvs();
    }
}
