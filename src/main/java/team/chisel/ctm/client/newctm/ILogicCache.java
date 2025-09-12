package team.chisel.ctm.client.newctm;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import team.chisel.ctm.client.newctm.CTMLogicBakery.OutputFace;

public interface ILogicCache {

    OutputFace[] getCachedSubmaps();

    long serialized();

    /**
     * Builds the connection map and stores it in this CTM instance.
     */
    void buildConnectionMap(IBlockAccess world, BlockPos pos, EnumFacing side);
}
