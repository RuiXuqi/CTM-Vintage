package team.chisel.ctm.api.util;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import lombok.EqualsAndHashCode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.client.util.IdentityStrategy;
import team.chisel.ctm.client.util.ProfileUtil;
import team.chisel.ctm.client.util.RegionCache;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * List of IBlockRenderContext's
 */
@EqualsAndHashCode(of = "serialized")
@ParametersAreNonnullByDefault
public class RenderContextList {

    private static final ThreadLocal<RegionCache> regionMetaCache = ThreadLocal.withInitial(
            () -> new RegionCache(BlockPos.ORIGIN, 0, null));

    private final Map<ICTMTexture<?>, ITextureContext> contextMap = Maps.newIdentityHashMap();
    private final Object2LongMap<ICTMTexture<?>> serialized = new Object2LongOpenCustomHashMap<>(new IdentityStrategy<>());

    public RenderContextList(IBlockState state, Collection<ICTMTexture<?>> textures, final IBlockAccess world, BlockPos pos) {
        ProfileUtil.start("ctm_region_cache_update");
        IBlockAccess cachedWorld = regionMetaCache.get().updateWorld(world);

        ProfileUtil.endAndStart("ctm_context_gather");
        for (ICTMTexture<?> tex : textures) {
            ITextureType type = tex.getType();
            ITextureContext ctx = type.getBlockRenderContext(state, cachedWorld, pos, tex);
            if (ctx != null) {
                this.contextMap.put(tex, ctx);
            }
        }

        ProfileUtil.endAndStart("ctm_context_serialize");
        for (Entry<ICTMTexture<?>, ITextureContext> e : this.contextMap.entrySet()) {
            this.serialized.put(e.getKey(), e.getValue().getCompressedData());
        }
        ProfileUtil.end();
    }

    public @Nullable ITextureContext getRenderContext(ICTMTexture<?> tex) {
        return this.contextMap.get(tex);
    }

    public boolean contains(ICTMTexture<?> tex) {
        return this.getRenderContext(tex) != null;
    }

    public Object2LongMap<ICTMTexture<?>> serialized() {
        return this.serialized;
    }
}
