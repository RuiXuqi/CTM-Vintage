package team.chisel.ctm.client.texture.ctx;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import team.chisel.ctm.api.texture.ITextureContext;

import javax.annotation.Nonnull;

public class TextureContextPosition implements ITextureContext {

    protected @Nonnull BlockPos position;

    public TextureContextPosition(@Nonnull BlockPos pos) {
        this.position = pos.toImmutable();
    }

    public TextureContextPosition(int x, int y, int z) {
        this(new BlockPos(x, y, z));
    }

    public TextureContextPosition applyOffset(IBlockAccess world) {
        this.position = position.add(OffsetProviderRegistry.INSTANCE.getOffset((World) world, position));
        return this;
    }

    public @Nonnull BlockPos getPosition() {
        return position;
    }

    @Override
    public long getCompressedData() {
        return 0L; // Position data is not useful for serialization (and in fact breaks caching as each location is a new key)
    }
}
