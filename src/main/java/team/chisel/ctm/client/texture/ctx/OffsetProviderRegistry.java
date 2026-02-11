package team.chisel.ctm.client.texture.ctx;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import team.chisel.ctm.api.IOffsetProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public enum OffsetProviderRegistry {

    INSTANCE;

    private final List<IOffsetProvider> providers = new ArrayList<>();

    public void registerProvider(IOffsetProvider provider) {
        this.providers.add(provider);
    }

    public BlockPos getOffset(IBlockAccess world, BlockPos pos) {
        BlockPos ret = BlockPos.ORIGIN;
        for (IOffsetProvider p : this.providers) {
            ret = ret.add(p.getOffset(world, pos));
        }
        return ret;
    }

}
