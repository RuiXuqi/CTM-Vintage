package team.chisel.ctm.client.newctm;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.util.TextureInfo;

import java.util.List;

public class TextureTypeCustom implements ITextureType {

    private final CustomCTMLogic logic;

    public TextureTypeCustom(CustomCTMLogic customLogic) {
        this.logic = customLogic;
    }

    @Override
    public @NotNull ITextureContext getBlockRenderContext(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull ICTMTexture<?> tex) {
        return new TextureContextCustomCTM(state, world, pos, tex, logic);
    }

    @Override
    public @NotNull ITextureContext getContextFromData(long data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NotNull List<ISubmap> getOutputFaces() {
        return logic.outputSubmaps();
    }

    @Override
    public int requiredTextures() {
        return logic.requiredTextures();
    }

    @Override
    public @NotNull TextureCustomCTM<? extends TextureTypeCustom> makeTexture(@NotNull TextureInfo info) {
        return new TextureCustomCTM<>(this, info);
    }

    public ISubmap getFallbackUvs() {
        return logic.getFallbackUvs();
    }

    public CTMLogicBakery.OutputFace getFallbackFace() {
        return logic.getFallbackFace();
    }
}
