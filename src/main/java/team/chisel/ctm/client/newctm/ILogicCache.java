package team.chisel.ctm.client.newctm;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import team.chisel.ctm.client.newctm.CTMLogicBakery.OutputFace;
import team.chisel.ctm.client.util.Dir;

public interface ILogicCache {

    OutputFace[] getCachedSubmaps();

    long serialized();

    /**
     * Builds the connection map and stores it in this CTM instance. The {@link #connected(Dir)}, {@link #connectedAnd(Dir...)}, and {@link #connectedOr(Dir...)} methods can be used to access it.
     */
    void buildConnectionMap(IBlockAccess world, BlockPos pos, EnumFacing side);
}