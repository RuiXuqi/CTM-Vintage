package team.chisel.ctm.client.newctm;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.client.newctm.CTMLogicBakery.OutputFace;
import team.chisel.ctm.client.util.Submap;

import javax.annotation.Nullable;
import java.util.List;

public interface ICTMLogic {

    int[] getSubmapIds(IBlockAccess world, BlockPos pos, EnumFacing side);

    OutputFace[] getSubmaps(IBlockAccess world, BlockPos pos, EnumFacing side);

    ILogicCache cached(@Nullable ConnectionCheck connectionCheck);

    List<ISubmap> outputSubmaps();

    default ISubmap getFallbackUvs() {
        return Submap.X1;
    }

    int requiredTextures();

}
