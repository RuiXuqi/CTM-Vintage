package team.chisel.ctm.client.newctm;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;

import javax.annotation.Nonnull;
import java.util.EnumMap;

public class TextureContextCustomCTM implements ITextureContext {

    protected final ICTMTexture<?> tex;

    private final ICTMLogic logic;

    private final EnumMap<EnumFacing, ILogicCache> ctmData = new EnumMap<>(EnumFacing.class);

    private long data;

    public TextureContextCustomCTM(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos, ICTMTexture<?> tex, ICTMLogic logic) {
        this.tex = tex;
        this.logic = logic;

        for (EnumFacing face : EnumFacing.values()) {
            ILogicCache ctm = createCTM(state);
            ctm.buildConnectionMap(world, pos, face);
            ctmData.put(face, ctm);
            this.data |= ctm.serialized() << (face.ordinal() * 10);
        }
    }

    protected ILogicCache createCTM(@Nonnull IBlockState state) {
        return logic.cached();
    }

    public ILogicCache getCTM(EnumFacing face) {
        return ctmData.get(face);
    }

    @Override
    public long getCompressedData() {
        return this.data;
    }
}
