package team.chisel.ctm.client.texture.ctx;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.client.texture.render.TextureCTM;
import team.chisel.ctm.client.util.CTMLogic;

import java.util.EnumMap;

public class TextureContextCTM implements ITextureContext {

    protected final TextureCTM<?> tex;

    private final EnumMap<EnumFacing, CTMLogic> ctmData = new EnumMap<>(EnumFacing.class);

    private long data;

    public TextureContextCTM(@NotNull IBlockState state, IBlockAccess world, BlockPos pos, TextureCTM<?> tex) {
        this.tex = tex;

        for (EnumFacing face : EnumFacing.VALUES) {
            CTMLogic ctm = this.createCTM(state);
            ctm.getSubmapIds(world, pos, state, face);
            this.ctmData.put(face, ctm);
            this.data |= ctm.serialized() << (face.ordinal() * 10);
        }
    }

    protected CTMLogic createCTM(@NotNull IBlockState state) {
        CTMLogic ret = CTMLogic.getInstance();
        this.tex.applyTo(ret.connectionCheck);
        return ret;
    }

    public CTMLogic getCTM(EnumFacing face) {
        return this.ctmData.get(face);
    }

    @Override
    public long getCompressedData() {
        return this.data;
    }
}
