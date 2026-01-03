package team.chisel.ctm.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface IOffsetProvider {

    /**
     * @deprecated Implement {@link #getOffset(IBlockAccess, BlockPos)}.
     */
    @NotNull
    @Deprecated
    default BlockPos getOffset(@NotNull World world, @NotNull BlockPos pos) {
        return BlockPos.ORIGIN;
    }

    @NotNull
    default BlockPos getOffset(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        if (world instanceof World realWorld) {
            return getOffset(realWorld, pos);
        }
        return BlockPos.ORIGIN;
    }
}
