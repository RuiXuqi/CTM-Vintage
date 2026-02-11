package team.chisel.ctm.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * To be implemented on blocks that "hide" another block inside, so connected textures can still be accomplished.
 */
@SuppressWarnings("unused")
public interface IFacade {

    /**
     * @deprecated Use {@link #getFacade(IBlockAccess, BlockPos, EnumFacing, BlockPos)}
     */
    @NotNull
    @Deprecated
    IBlockState getFacade(@NotNull IBlockAccess world, @NotNull BlockPos pos, @Nullable EnumFacing side);

    /**
     * Gets the blockstate this facade appears as.
     *
     * @param world      {@link World}
     * @param pos        The Blocks position
     * @param side       The side being rendered, NOT the side being connected from.
     *                   <p>
     *                   This value can be null if no side is specified. Please handle this appropriately.
     * @param connection The position of the block being connected to.
     * @return The blockstate which your block appears as.
     */
    @NotNull
    default IBlockState getFacade(@NotNull IBlockAccess world, @NotNull BlockPos pos, @Nullable EnumFacing side, @NotNull BlockPos connection) {
        return this.getFacade(world, pos, side);
    }

    /**
     * Gets the blockstate this facade appears as.
     *
     * @param world           {@link World}
     * @param pos             The Blocks position
     * @param side            The side being rendered, NOT the side being connected from.
     *                        <p>
     *                        This value can be null if no side is specified. Please handle this appropriately.
     * @param connectionState The blockstate of the block being connected to.
     * @param connection      The position of the block being connected to.
     * @return The blockstate which your block appears as.
     */
    @NotNull
    default IBlockState getFacade(@NotNull IBlockAccess world, @NotNull BlockPos pos, @Nullable EnumFacing side, @NotNull IBlockState connectionState, @NotNull BlockPos connection) {
        return this.getFacade(world, pos, side, connection);
    }

}
