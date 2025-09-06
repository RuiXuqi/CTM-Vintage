package team.chisel.ctm.client.newctm;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface LocalDirection {

    /**
     * Finds if this block is connected for the given side in this Dir.
     *
     * @param ctm   The CTM instance to use for logic.
     * @param world The world the block is in.
     * @param pos   The position of your block.
     * @param side  The side of the current face.
     * @return True if the block is connected in the given Dir, false otherwise.
     */
    boolean isConnected(ConnectionCheck ctm, IBlockAccess world, BlockPos pos, EnumFacing side);

    /**
     * Finds if this block is connected for the given side in this Dir.
     *
     * @param ctm   The CTM instance to use for logic.
     * @param world The world the block is in.
     * @param pos   The position of your block.
     * @param side  The side of the current face.
     * @param state The state to check for connection with.
     * @return True if the block is connected in the given Dir, false otherwise.
     */
    boolean isConnected(ConnectionCheck ctm, IBlockAccess world, BlockPos pos, EnumFacing side, IBlockState state);

    LocalDirection relativize(EnumFacing normal);

    BlockPos getOffset(EnumFacing normal);

    int ordinal();

    String name();

    String asJson();
}