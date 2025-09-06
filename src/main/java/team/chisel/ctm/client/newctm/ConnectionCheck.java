package team.chisel.ctm.client.newctm;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import team.chisel.ctm.Configurations;
import team.chisel.ctm.api.IFacade;
import team.chisel.ctm.client.util.CTMLogic.StateComparisonCallback;

import javax.annotation.Nullable;
import java.util.Optional;

@Accessors(fluent = true, chain = true)
public class ConnectionCheck {

    public Optional<Boolean> disableObscuredFaceCheck = Optional.empty();

    @Getter
    @Setter
    protected boolean ignoreStates, actualStates;

    @Getter
    @Setter
    protected StateComparisonCallback stateComparator = StateComparisonCallback.DEFAULT;

    /**
     * A simple check for if the given block can connect to the given direction on the given side.
     *
     * @param world
     * @param current    The position of your block.
     * @param connection The position of the block to check against.
     * @param dir        The {@link EnumFacing side} of the block to check for connection status. This is <i>not</i> the direction to check in.
     * @return True if the given block can connect to the given location on the given side.
     */
    public final boolean isConnected(IBlockAccess world, BlockPos current, BlockPos connection, EnumFacing dir) {

        IBlockState state = getConnectionState(world, current, dir, connection);
        return isConnected(world, current, connection, dir, state);
    }

    /**
     * A simple check for if the given block can connect to the given direction on the given side.
     *
     * @param world
     * @param current    The position of your block.
     * @param connection The position of the block to check against.
     * @param dir        The {@link EnumFacing side} of the block to check for connection status. This is <i>not</i> the direction to check in.
     * @param state      The state to check against for connection.
     * @return True if the given block can connect to the given location on the given side.
     */
    @SuppressWarnings({"unused", "null"})
    public boolean isConnected(IBlockAccess world, BlockPos current, BlockPos connection, EnumFacing dir, IBlockState state) {

//      if (CTMLib.chiselLoaded() && connectionBlocked(world, x, y, z, dir.ordinal())) {
//          return false;
//      }

        BlockPos obscuringPos = connection.offset(dir);

        boolean disableObscured = disableObscuredFaceCheck.orElse(Configurations.connectInsideCTM);

        IBlockState con = getConnectionState(world, connection, dir, current);
        IBlockState obscuring = disableObscured ? null : getConnectionState(world, obscuringPos, dir, current);

        // bad API user
        if (con == null) {
            throw new IllegalStateException("Error, received null blockstate as facade from block " + world.getBlockState(connection));
        }

        boolean ret = stateComparator(state, con, dir);

        // no block obscuring this face
        if (obscuring == null) {
            return ret;
        }

        // check that we aren't already connected outwards from this side
        ret &= !stateComparator(state, obscuring, dir);

        return ret;
    }

    public boolean stateComparator(IBlockState from, IBlockState to, EnumFacing dir) {
        return stateComparator.connects(this, from, to, dir);
    }

//    private boolean connectionBlocked(IBlockReader world, int x, int y, int z, int side) {
//        Block block = world.getBlock(x, y, z);
//        if (block instanceof IConnectable) {
//            return !((IConnectable) block).canConnectCTM(world, x, y, z, side);
//        }
//        return false;
//    }

    public IBlockState getConnectionState(IBlockAccess world, BlockPos pos, @Nullable EnumFacing side, BlockPos connection) {
        IBlockState state = world.getBlockState(pos);
        if (actualStates()) {
            state = state.getActualState(world, pos);
        }
        if (state.getBlock() instanceof IFacade) {
            return ((IFacade) state.getBlock()).getFacade(world, pos, side, connection);
        }
        return state;
    }

}