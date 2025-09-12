package team.chisel.ctm.client.util;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FaceOffset {
    public static BlockPos getBlockPosOffsetFromFaceOffset(EnumFacing facing, int xOffset, int yOffset) {
        return switch (facing) {
            // UP
            default -> new BlockPos(xOffset, 0, -yOffset);
            case DOWN -> new BlockPos(xOffset, 0, yOffset);
            case NORTH -> new BlockPos(-xOffset, yOffset, 0);
            case SOUTH -> new BlockPos(xOffset, yOffset, 0);
            case WEST -> new BlockPos(0, yOffset, xOffset);
            case EAST -> new BlockPos(0, yOffset, -xOffset);
        };
    }
}
