package team.chisel.ctm.client.texture.type;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextCTM;
import team.chisel.ctm.client.texture.render.TextureCTM;
import team.chisel.ctm.client.texture.render.TextureSCTM;
import team.chisel.ctm.client.util.CTMLogic;

import java.util.Optional;

@TextureType("sctm")
@TextureType("ctm_simple")
public class TextureTypeSCTM extends TextureTypeCTM {

    @Override
    public ICTMTexture<TextureTypeSCTM> makeTexture(@NotNull TextureInfo info) {
        return new TextureSCTM(this, info);
    }

    @Override
    public TextureContextCTM getBlockRenderContext(final @NotNull IBlockState state, final @NotNull IBlockAccess world, final @NotNull BlockPos pos, final @NotNull ICTMTexture<?> tex) {
        return new TextureContextCTM(state, world, pos, (TextureCTM<?>) tex) {

            @Override
            protected CTMLogic createCTM(@NotNull IBlockState state) {
                CTMLogic ctm = super.createCTM(state);

                ctm.connectionCheck.disableObscuredFaceCheck = Optional.of(true);

                return ctm;
            }
        };
    }

    @Override
    public int getQuadsPerSide() {
        return 1;
    }

    @Override
    public int requiredTextures() {
        return 1;
    }
}
