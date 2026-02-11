package team.chisel.ctm.client.texture.render;

import com.github.bsideup.jabel.Desugar;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.Configurations;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.newctm.ConnectionCheck;
import team.chisel.ctm.client.newctm.ITextureConnection;
import team.chisel.ctm.client.texture.ctx.TextureContextCTM;
import team.chisel.ctm.client.texture.type.TextureTypeCTM;
import team.chisel.ctm.client.util.BlockstatePredicateParser;
import team.chisel.ctm.client.util.CTMLogic;
import team.chisel.ctm.client.util.CTMLogic.StateComparisonCallback;
import team.chisel.ctm.client.util.ParseUtils;
import team.chisel.ctm.client.util.Quad;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@Accessors(fluent = true)
public class TextureCTM<T extends TextureTypeCTM> extends AbstractTexture<T> implements ITextureConnection {

    private static final BlockstatePredicateParser predicateParser = new BlockstatePredicateParser();

    @Getter
    private final Optional<Boolean> connectInside;

    @Getter
    private final boolean ignoreStates, actualStates;

    @Nullable
    private final BiPredicate<EnumFacing, IBlockState> connectionChecks;

    @Desugar
    private record CacheKey(IBlockState from, EnumFacing dir) {

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.dir.hashCode();
            result = prime * result + System.identityHashCode(this.from);
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            return this.dir == other.dir && this.from == other.from;
        }
    }

    private final Cache<CacheKey, Object2ByteMap<IBlockState>> connectionCache = CacheBuilder.newBuilder().build();

    public TextureCTM(T type, TextureInfo info) {
        super(type, info);
        this.connectInside = info.getInfo().flatMap(obj -> ParseUtils.getBoolean(obj, "connect_inside"));
        this.ignoreStates = info.getInfo().map(obj -> JsonUtils.getBoolean(obj, "ignore_states", false)).orElse(false);
        this.actualStates = info.getInfo().map(obj -> JsonUtils.getBoolean(obj, "use_actual_state", false)).orElse(false);
        this.connectionChecks = info.getInfo().map(obj -> predicateParser.parse(obj.get("connect_to"))).orElse(null);
    }

    @Override
    public boolean connectTo(ConnectionCheck ctm, IBlockState from, IBlockState to, EnumFacing dir) {
        try {
            return ((this.connectionChecks == null ? StateComparisonCallback.DEFAULT.connects(ctm, from, to, dir) : this.connectionChecks.test(dir, to) && this.connectionChecks.test(dir, from)) ? 1 : 0) == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad bq, @Nullable ITextureContext context, int quadGoal) {
        Quad quad = this.makeQuad(bq, context);
        if (context == null || Configurations.disableCTM) {
            return Collections.singletonList(quad.transformUVs(this.sprites[0]).rebake());
        }

        Quad[] quads = quad.subdivide(4);

        int[] ctm = ((TextureContextCTM) context).getCTM(bq.getFace()).getSubmapIndices();
        //CTM.logger.info("{}: {}", bq.getDirection(), Arrays.toString(ctm));

        for (int i = 0; i < quads.length; i++) {
            Quad q = quads[i];
            if (q != null) {
                int ctmid = q.getUvs().normalize().getQuadrant();
//              quads[i] = q.grow().transformUVs(sprites[1], CTMLogic.uvs[16]);

                quads[i] = q.grow().transformUVs(this.sprites[ctm[ctmid] > 15 ? 0 : 1], CTMLogic.uvs[ctm[ctmid]].unitScale());
            }
        }
        return Arrays.stream(quads).filter(Objects::nonNull).map(Quad::rebake).collect(Collectors.toList());
    }

    @Override
    protected Quad makeQuad(BakedQuad bq, @Nullable ITextureContext context) {
        return super.makeQuad(bq, context).derotate();
    }
}
