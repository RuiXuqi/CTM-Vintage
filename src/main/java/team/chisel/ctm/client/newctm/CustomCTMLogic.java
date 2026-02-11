package team.chisel.ctm.client.newctm;

import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.client.newctm.CTMLogicBakery.OutputFace;
import team.chisel.ctm.client.util.Submap;

import java.util.*;

@RequiredArgsConstructor
public class CustomCTMLogic implements ICTMLogic {

    @VisibleForTesting
    public final int[][] lookups;
    private final OutputFace[] tiles;
    private final LocalDirection[] directions;
    private final ConnectionCheck connectionCheck = new ConnectionCheck();

    private class Cache implements ILogicCache {

        @Nullable
        private final ConnectionCheck connectionCheckOverride;
        private int[] cachedSubmapIds;
        private OutputFace[] cachedSubmaps;

        public Cache(@Nullable ConnectionCheck connectionCheck) {
            this.connectionCheckOverride = connectionCheck;
        }

        @Override
        public OutputFace[] getCachedSubmaps() {
            return this.cachedSubmaps;
        }

        @Override
        public long serialized() {
            int stride = CustomCTMLogic.this.directions.length;
            int len = this.cachedSubmapIds.length;
            if (len * stride > 64) {
                throw new IllegalStateException("Too many submaps to serialize");
            }
            long ret = 0L;
            for (int i = 0; i < this.cachedSubmapIds.length; i++) {
                ret |= ((long) this.cachedSubmapIds[i]) << (i * stride);
            }
            return ret;
        }

        @Override
        public void buildConnectionMap(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
            this.cachedSubmapIds = CustomCTMLogic.this.getSubmapIds(world, pos, state, side, this.connectionCheckOverride);
            //Manually call with the computed submap ids to avoid having to calculate them a second type
            // like getSubmaps(IBlockAccess, BlockPos, EnumFacing) needs to do, and allows us to use
            // data that is based on our connection check override
            this.cachedSubmaps = CustomCTMLogic.this.getSubmaps(this.cachedSubmapIds);
        }
    }

    @Override
    public int[] getSubmapIds(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        return this.getSubmapIds(world, pos, state, side, this.connectionCheck);
    }

    private int[] getSubmapIds(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side, ConnectionCheck connectionCheck) {
        int key = 0;
        for (int i = 0; i < this.directions.length; i++) {
            boolean isConnected = this.directions[i].isConnected(connectionCheck, world, pos, state, side);
            key |= (isConnected ? 1 : 0) << i;
        }
        if (key >= this.lookups.length || this.lookups[key] == null) {
            throw new IllegalStateException("Input state found that is not in lookup table: " + Integer.toBinaryString(key));
        }
        return this.lookups[key];
    }

    @Override
    public OutputFace[] getSubmaps(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        var tileIds = this.getSubmapIds(world, pos, state, side);
        return this.getSubmaps(tileIds);
    }

    private OutputFace[] getSubmaps(int[] tileIds) {
        OutputFace[] ret = new OutputFace[tileIds.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = this.tiles[tileIds[i]];
        }
        return ret;
    }

    @Override
    public ILogicCache cached(@Nullable ConnectionCheck connectionCheck) {
        return this.new Cache(connectionCheck);
    }

    private List<ISubmap> outputSubmapCache;

    @Override
    public List<ISubmap> outputSubmaps() {
        if (this.outputSubmapCache == null) {
            Set<ISubmap> seen = new HashSet<>();
            for (var tile : this.tiles) {
                seen.add(tile.face());
            }
            this.outputSubmapCache = new ArrayList<>(seen);
        }
        return this.outputSubmapCache;
    }

    @Override
    public ISubmap getFallbackUvs() {
        return this.tiles.length == 0 ? ICTMLogic.super.getFallbackUvs() : this.tiles[0].uvs();
    }

    private int textureCountCache = -1;

    @Override
    public int requiredTextures() {
        if (this.textureCountCache < 0) {
            BitSet seen = new BitSet();
            for (var tile : this.tiles) {
                seen.set(tile.tex());
            }
            this.textureCountCache = seen.cardinality();
        }
        return this.textureCountCache;
    }

    public OutputFace getFallbackFace() {
        return this.tiles.length > 0 ? this.tiles[0] : new OutputFace(0, ICTMLogic.super.getFallbackUvs(), Submap.X1);
    }
}
