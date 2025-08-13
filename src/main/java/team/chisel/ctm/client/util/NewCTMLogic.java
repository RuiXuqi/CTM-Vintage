package team.chisel.ctm.client.util;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

@RequiredArgsConstructor
public class NewCTMLogic {

    private final int[][] lookups;
    private final Submap[] tiles;
    private final LocalDirection[] directions;
    private final ConnectionCheck connectionCheck;

    public Submap[] getSubmaps(IBlockAccess world, BlockPos pos, EnumFacing side) {
        int key = 0;
        for (int i = 0; i < directions.length; i++) {
            BlockPos offset = directions[i].getOffset(side);
            BlockPos conPos = pos.add(offset.getX(), offset.getY(), offset.getZ());
            key |= (connectionCheck.isConnected(world, pos, conPos, side) ? 1 : 0) << i;
        }
        if (key >= lookups.length) {
            throw new IllegalStateException("Input state found that is not in lookup table: " + Integer.toBinaryString(key));
        }
        // TODO reduce allocation by encapsulating multi-submap results
        int[] tileIds = lookups[key];
        Submap[] ret = new Submap[tileIds.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = tiles[tileIds[i]];
        }
        return ret;
    }

}