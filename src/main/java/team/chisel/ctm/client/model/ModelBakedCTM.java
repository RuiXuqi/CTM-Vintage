package team.chisel.ctm.client.model;

import com.google.common.collect.ObjectArrays;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.model.IModelCTM;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.util.RenderContextList;
import team.chisel.ctm.client.util.BakedQuadRetextured;
import team.chisel.ctm.client.util.Lazy;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ModelBakedCTM extends AbstractCTMBakedModel {

    public ModelBakedCTM(IModelCTM model, IBakedModel parent, @Nullable BlockRenderLayer layer) {
        super(model, parent, layer);
    }

    private static final EnumFacing[] FACINGS = ObjectArrays.concat(EnumFacing.VALUES, (EnumFacing) null);

    @Override
    protected AbstractCTMBakedModel createModel(@Nullable IBlockState state, @NotNull IModelCTM model, IBakedModel parent, @Nullable RenderContextList ctx, long rand, @Nullable BlockRenderLayer layer) {
        while (parent instanceof ModelBakedCTM castParent) {
            parent = castParent.getParent(rand);
        }
        final IBakedModel finalParent = parent;

        // Calculate this lazily to avoid redundant checks during render
        Lazy<Boolean> layerMatches = Lazy.of(() -> {
            if (layer == null) return true; // Item render
            if (state != null) return state.getBlock().canRenderInLayer(state, layer);
            return true;
        });

        ModelBakedCTM ret = new ModelBakedCTM(model, parent, layer);
        for (EnumFacing facing : FACINGS) {
            List<BakedQuad> parentQuads = finalParent.getQuads(state, facing, rand);
            List<BakedQuad> destQuads;
            if (facing != null) {
                destQuads = ret.faceQuads.get(facing);
            } else {
                destQuads = ret.genQuads;
            }

            // Linked to maintain the order of quads
            Map<BakedQuad, ICTMTexture<?>> texturemap = new LinkedHashMap<>();
            // Gather all quads and map them to their textures
            // All quads should have an associated ICTMTexture, so ignore any that do not
            for (BakedQuad q : parentQuads) {
                if (q.getSprite() == null) continue;
                String iconName = q.getSprite().getIconName();
                int tintIndex = q.getTintIndex();

                ICTMTexture<?> tex = this.getOverrideTexture(rand, tintIndex, iconName);
                if (tex == null) {
                    tex = this.getTexture(rand, iconName);
                }

                if (tex != null) {
                    TextureAtlasSprite spriteReplacement = this.getOverrideSprite(rand, tintIndex);
                    if (spriteReplacement != null) {
                        q = new BakedQuadRetextured(q, spriteReplacement);
                    }

                    texturemap.put(q, tex);
                }
            }

            // Compute the quad goal for a given facing
            int quadGoal = ctx == null ? 1 : texturemap.values().stream().mapToInt(tex -> tex.getType().getQuadsPerSide()).max().orElse(1);
            for (Entry<BakedQuad, ICTMTexture<?>> e : texturemap.entrySet()) {
                ICTMTexture<?> texture = e.getValue();
                BlockRenderLayer texLayer = texture.getLayer();

                // If the layer is null, this is a wrapped vanilla texture, so passthrough the layer check to the block
                if (layer == null || (texLayer != null && texLayer == layer) || (texLayer == null && layerMatches.get())) {
                    ITextureContext tcx = ctx == null ? null : ctx.getRenderContext(texture);
                    destQuads.addAll(texture.transformQuad(e.getKey(), tcx, quadGoal));
                }
            }
        }
        return ret;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return wrapParticleIcon(super.getParticleTexture());
    }

    private @NotNull TextureAtlasSprite wrapParticleIcon(@NotNull TextureAtlasSprite particleIcon) {
        return Optional.ofNullable(getModel().getTexture(particleIcon.getIconName()))
                .map(ICTMTexture::getParticle)
                .orElse(particleIcon);
    }
}
