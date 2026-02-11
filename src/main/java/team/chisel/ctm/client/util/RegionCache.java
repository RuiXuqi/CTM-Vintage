package team.chisel.ctm.client.util;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;

/**
 * Used by render state creation to avoid unnecessary block lookups through the world.
 */
@ParametersAreNonnullByDefault
public class RegionCache implements IBlockAccess {

    /*
     * XXX
     *
     * These are required for future use, in case there is ever a need to have this region cache only store a certain area of the world.
     *
     * Currently, this class is only used by CTM, which is limited to a very small subsection of the world,
     * and thus the overhead of distance checking is unnecessary.
     */
    @SuppressWarnings("unused")
    private final BlockPos center;
    @SuppressWarnings("unused")
    private final int radius;

    private WeakReference<IBlockAccess> passthrough;
    private final Long2ObjectMap<IBlockState> stateCache = new Long2ObjectOpenHashMap<>();

    public RegionCache(BlockPos center, int radius, @Nullable IBlockAccess passthrough) {
        this.center = center;
        this.radius = radius;
        this.passthrough = new WeakReference<>(passthrough);
    }

    private IBlockAccess getPassthrough() {
        IBlockAccess ret = this.passthrough.get();
        Preconditions.checkNotNull(ret);
        return ret;
    }

    public @NotNull RegionCache updateWorld(IBlockAccess passthrough) {
        // We do NOT use getPassthrough() here so as to skip the null-validation - it's obviously valid to be null here
        if (this.passthrough.get() != passthrough) {
            this.stateCache.clear();
            this.passthrough = new WeakReference<>(passthrough);
        }
        return this;
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        return this.getPassthrough().getTileEntity(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        // In cases with direct passthroughs, these are never used by our code.
        // But in case something out there does use them, this will work
        return this.getPassthrough().getCombinedLight(pos, lightValue);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        long address = pos.toLong();
        var state = this.stateCache.get(address);

        if (state == null) {
            state = this.getPassthrough().getBlockState(pos);
            this.stateCache.put(address, state);
        }

        return state;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        IBlockState state = this.getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.getPassthrough().getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return this.getPassthrough().getStrongPower(pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return this.getPassthrough().getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return this.getPassthrough().isSideSolid(pos, side, _default);
    }
}
