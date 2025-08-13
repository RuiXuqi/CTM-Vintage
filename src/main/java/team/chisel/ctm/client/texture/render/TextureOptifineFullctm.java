package team.chisel.ctm.client.texture.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.var;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import team.chisel.ctm.Configurations;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextOptifineFullctm;
import team.chisel.ctm.client.texture.type.TextureTypeOptifineFullctm;
import team.chisel.ctm.client.util.*;
import team.chisel.ctm.client.util.CTMLogic.StateComparisonCallback;
import team.chisel.ctm.client.util.CTMLogicBakery.OutputFace;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@ParametersAreNonnullByDefault
@Accessors(fluent = true)
public class TextureOptifineFullctm<T extends TextureTypeOptifineFullctm> extends AbstractTexture<T> {

	private static final BlockstatePredicateParser predicateParser = new BlockstatePredicateParser();

	@Getter
	private final Optional<Boolean> connectInside;

	@Getter
	private final boolean ignoreStates;

	@Nullable private final BiPredicate<EnumFacing, IBlockState> connectionChecks;

	@RequiredArgsConstructor
	private static final class CacheKey {
		private final IBlockState from;
		private final EnumFacing dir;

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
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (dir != other.dir)
				return false;
			if (from != other.from)
				return false;
			return true;
		}
	}

	private final Cache<CacheKey, Object2ByteMap<IBlockState>> connectionCache = CacheBuilder.newBuilder().build();

	public TextureOptifineFullctm(T type, TextureInfo info) {
		super(type, info);
		this.connectInside = info.getInfo().flatMap(obj -> ParseUtils.getBoolean(obj, "connect_inside"));
		this.ignoreStates = info.getInfo().map(obj -> JsonUtils.getBoolean(obj, "ignore_states", false)).orElse(false);
		this.connectionChecks = info.getInfo().map(obj -> predicateParser.parse(obj.get("connect_to"))).orElse(null);
	}

	public boolean connectTo(ConnectionCheck ctm, IBlockState from, IBlockState to, EnumFacing dir) {
		try {
			return ((connectionChecks == null ? StateComparisonCallback.DEFAULT.connects(ctm, from, to, dir) : connectionChecks.test(dir, to)) ? 1 : 0) == 1;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<BakedQuad> transformQuad(BakedQuad bq, ITextureContext context, int quadGoal) {
		Quad quad = makeQuad(bq, context);
		if (context == null || Configurations.disableCTM) {
			return Collections.singletonList(quad.transformUVs(sprites[0], Submap.X1).rebake()); // default tex
		}

		OutputFace[] ctm = ((TextureContextOptifineFullctm)context).getCTM(bq.getFace()).getCachedSubmaps();
		List<BakedQuad> ret = new ArrayList<>();
		for (var face : ctm) {
			Quad sub = quad.subsect(face.getFace());
			if (sub != null) {
				ret.add(sub.transformUVs(sprites[face.getSprite()], face.getUvs()).rebake());
			}
		}
		return ret;
	}

	@Override
	protected Quad makeQuad(BakedQuad bq, ITextureContext context) {
		return super.makeQuad(bq, context).derotate();
	}
}
