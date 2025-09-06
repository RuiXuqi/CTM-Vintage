package team.chisel.ctm.client.newctm;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.client.newctm.CTMLogicBakery.OutputFace;

import java.util.List;

public interface ICTMLogic {

    int[] getSubmapIds(IBlockAccess world, BlockPos pos, EnumFacing side);

    OutputFace[] getSubmaps(IBlockAccess world, BlockPos pos, EnumFacing side);

    ILogicCache cached();

    List<ISubmap> outputSubmaps();

    int requiredTextures();

}