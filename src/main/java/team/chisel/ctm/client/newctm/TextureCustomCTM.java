package team.chisel.ctm.client.newctm;

import com.github.bsideup.jabel.Desugar;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.Configurations;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.newctm.CTMLogicBakery.OutputFace;
import team.chisel.ctm.client.texture.render.AbstractTexture;
import team.chisel.ctm.client.util.*;
import team.chisel.ctm.client.util.CTMLogic.StateComparisonCallback;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@ParametersAreNonnullByDefault
@Accessors(fluent = true)
public class TextureCustomCTM<T extends TextureTypeCustom> extends AbstractTexture<T> implements ITextureConnection {

    private static final BlockstatePredicateParser predicateParser = new BlockstatePredicateParser();

    @Getter
    private final Optional<Boolean> connectInside;

    @Getter
    private final boolean ignoreStates, actualStates;

    @Nullable
    private final BiPredicate<EnumFacing, IBlockState> connectionChecks;

    private final TextureAtlasSprite particleSprite;

    @Desugar
    private record CacheKey(IBlockState from, EnumFacing dir) {

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + dir.hashCode();
            result = prime * result + System.identityHashCode(from);
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            return dir == other.dir && from == other.from;
        }
    }

    public TextureCustomCTM(T type, TextureInfo info) {
        super(type, info);
        this.connectInside = info.getInfo().flatMap(obj -> ParseUtils.getBoolean(obj, "connect_inside"));
        this.ignoreStates = info.getInfo().map(obj -> JsonUtils.getBoolean(obj, "ignore_states", false)).orElse(false);
        this.actualStates = info.getInfo().map(obj -> JsonUtils.getBoolean(obj, "use_actual_state", false)).orElse(false);
        this.connectionChecks = info.getInfo().map(obj -> predicateParser.parse(obj.get("connect_to"))).orElse(null);
        //Crop the particle sprite so that it only contains the bit it should
        this.particleSprite = PartialTextureAtlasSprite.createPartial(super.getParticle(), getFallbackUvs());
    }

    @Override
    public boolean connectTo(ConnectionCheck ctm, IBlockState from, IBlockState to, EnumFacing dir) {
        try {
            return ((connectionChecks == null ? StateComparisonCallback.DEFAULT.connects(ctm, from, to, dir) : connectionChecks.test(dir, to) && connectionChecks.test(dir, from)) ? 1 : 0) == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull TextureAtlasSprite getParticle() {
        return this.particleSprite;
    }

    private ISubmap getFallbackUvs() {
        return sprites.length == 1 ? type.getFallbackUvs() : Submap.X1;
    }

    @Override
    public @NotNull List<BakedQuad> transformQuad(BakedQuad bq, @Nullable ITextureContext context, int quadGoal) {
        Quad quad = makeQuad(bq, context);
        if (context == null || Configurations.disableCTM) {
            return Collections.singletonList(quad.setUVs(sprites[0], getFallbackUvs()).rebake());
        }

        OutputFace[] ctm = ((TextureContextCustomCTM) context).getCTM(bq.getFace()).getCachedSubmaps();
        List<BakedQuad> ret = new ArrayList<>();
        for (var face : ctm) {
            //CTM.logger.info("{}\t{}: {}@ {}", bq.getDirection(), face.getFace(), face.getTex(), face.getUvs());
            Quad sub = quad.subsect(face.face());
            ret.add(sub.setUVs(sprites[face.tex()], face.uvs()).rebake());
        }
        return ret;
    }

    @Override
    protected @NotNull Quad makeQuad(BakedQuad bq, @Nullable ITextureContext context) {
        return super.makeQuad(bq, context).derotate();
    }
}
