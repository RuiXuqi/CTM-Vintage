package team.chisel.ctm.client.texture.ctx;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.client.util.CTMLogicBakery;
import team.chisel.ctm.client.util.NewCTMLogic;

import javax.annotation.Nonnull;
import java.util.EnumMap;

public class TextureContextNewCTM implements ITextureContext {
    
	protected final ICTMTexture<?> tex;
	
    private EnumMap<EnumFacing, NewCTMLogic> ctmData = new EnumMap<>(EnumFacing.class);

    private long data;

    public TextureContextNewCTM(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos, ICTMTexture<?> tex) {
    	this.tex = tex;
    	
        for (EnumFacing face : EnumFacing.values()) {
            NewCTMLogic ctm = createCTM(state);
            ctm.getSubmaps(world, pos, face);
            ctmData.put(face, ctm);
            this.data |= ctm.serialized() << (face.ordinal() * 10);
        }
    }
    
    protected NewCTMLogic createCTM(@Nonnull IBlockState state) {
        return CTMLogicBakery.TEST_OF.bake();
    }

    public NewCTMLogic getCTM(EnumFacing face) {
        return ctmData.get(face);
    }

    @Override
    public long getCompressedData(){
        return this.data;
    }
}
