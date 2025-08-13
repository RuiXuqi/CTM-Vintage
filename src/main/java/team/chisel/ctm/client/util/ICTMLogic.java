package team.chisel.ctm.client.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface ICTMLogic {

    long serialized();

    /**
     * Builds the connection map and stores it in this CTM instance. The {@link #connected(Dir)}, {@link #connectedAnd(Dir...)}, and {@link #connectedOr(Dir...)} methods can be used to access it.
     */
    void buildConnectionMap(IBlockAccess world, BlockPos pos, EnumFacing side);

    void buildConnectionMap(long data, EnumFacing side);

    /**
     * @param dir
     *            The direction to check connection in.
     * @return True if the cached connectionMap holds a connection in this {@link Dir direction}.
     */
    boolean connected(Dir dir);

    /**
     * @param dirs
     *            The directions to check connection in.
     * @return True if the cached connectionMap holds a connection in <i><b>all</b></i> the given {@link Dir directions}.
     */
    boolean connectedAnd(Dir... dirs);

    /**
     * @param dirs
     *            The directions to check connection in.
     * @return True if the cached connectionMap holds a connection in <i><b>one of</b></i> the given {@link Dir directions}.
     */
    boolean connectedOr(Dir... dirs);

    boolean connectedNone(Dir... dirs);

    boolean connectedOnly(Dir... dirs);

    int numConnections();

}
