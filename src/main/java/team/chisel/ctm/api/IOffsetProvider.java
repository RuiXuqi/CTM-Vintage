package team.chisel.ctm.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public interface IOffsetProvider {

    @Nonnull
    public BlockPos getOffset(@Nonnull World world, @Nonnull BlockPos pos);

}
