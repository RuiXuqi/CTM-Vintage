package team.chisel.ctm.client.newctm;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.client.newctm.CTMLogicBakery.OutputFace;
import team.chisel.ctm.client.util.Submap;

import java.util.List;

public interface ICTMLogic {

    int[] getSubmapIds(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side);

    OutputFace[] getSubmaps(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side);

    ILogicCache cached(@Nullable ConnectionCheck connectionCheck);

    List<ISubmap> outputSubmaps();

    default ISubmap getFallbackUvs() {
        return Submap.X1;
    }

    int requiredTextures();

}
