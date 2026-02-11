package team.chisel.ctm.client.model;

import com.github.bsideup.jabel.Desugar;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.ModelLoader;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.model.IModelCTM;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.IChiselFace;
import team.chisel.ctm.api.util.RenderContextList;
import team.chisel.ctm.client.asm.CTMCoreMethods;
import team.chisel.ctm.client.state.CTMExtendedState;
import team.chisel.ctm.client.util.ProfileUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.vecmath.Matrix4f;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class AbstractCTMBakedModel extends BakedModelWrapper<IBakedModel> {

    private static final Cache<ModelResourceLocation, AbstractCTMBakedModel> itemcache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .build();
    private static final Cache<State, AbstractCTMBakedModel> modelcache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .maximumSize(5000)
            .build();

    public static void invalidateCaches() {
        itemcache.invalidateAll();
        modelcache.invalidateAll();
    }

    @ParametersAreNonnullByDefault
    private class Overrides extends ItemOverrideList {

        public Overrides() {
            super(Lists.newArrayList());
        }

        @Override
        @SneakyThrows
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            Block block = null;
            if (stack.getItem() instanceof ItemBlock) {
                block = ((ItemBlock) stack.getItem()).getBlock();
            }
            final IBlockState state = block == null ? null : block.getDefaultState();

            if (!stack.isEmpty() && stack.getItem().hasCustomProperties()) { // Handle parent model's overrides
                @SuppressWarnings("deprecation") // Duplicate super logic, but called on the parent model overrides
                ResourceLocation location = AbstractCTMBakedModel.this.getParent().getOverrides().applyOverride(stack, world, entity);
                if (location != null) {
                    // Use the override's location as cache key
                    ModelResourceLocation overrideLoc = ModelLoader.getInventoryVariant(location.toString());
                    IBakedModel newParent = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(overrideLoc);
                    return itemcache.get(overrideLoc, () -> AbstractCTMBakedModel.this.withNewParent(newParent).createModel(state, AbstractCTMBakedModel.this.model, newParent, null, 0, null));
                }
            }

            ModelResourceLocation mrl = ModelUtil.getMesh(stack);
            if (mrl == null) {
                // this must be a missing/invalid model
                return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
            }
            return itemcache.get(mrl, () -> AbstractCTMBakedModel.this.createModel(state, AbstractCTMBakedModel.this.model, AbstractCTMBakedModel.this.getParent(0), null, 0, null));
        }
    }

    @Desugar
    private record State(@NotNull IBlockState cleanState, @Nullable Object2LongMap<ICTMTexture<?>> serializedContext,
                         @NotNull IBakedModel parent, @Nullable BlockRenderLayer layer) {

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            State other = (State) obj;
            return this.cleanState == other.cleanState && this.parent == other.parent && this.layer == other.layer && Objects.equals(this.serializedContext, other.serializedContext);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            // for some reason blockstates hash their properties, we only care about the identity hash
            result = prime * result + System.identityHashCode(this.cleanState);
            result = prime * result + (this.parent == null ? 0 : this.parent.hashCode());
            result = prime * result + (this.serializedContext == null ? 0 : this.serializedContext.hashCode());
            result = prime * result + (this.layer == null ? 0 : this.layer.hashCode());
            return result;
        }
    }

    @Getter
    private final @NotNull IModelCTM model;
    private final @NotNull Overrides overrides = new Overrides();

    private final @Nullable BlockRenderLayer layer;
    protected final List<BakedQuad> genQuads = new ArrayList<>();
    protected final ListMultimap<EnumFacing, BakedQuad> faceQuads = ArrayListMultimap.create();

    public AbstractCTMBakedModel(@NotNull IModelCTM model, IBakedModel parent, @Nullable BlockRenderLayer layer) {
        super(parent);
        this.model = model;
        this.layer = layer;
    }

    @Override
    @SneakyThrows
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (CTMCoreMethods.renderingDamageModel.get()) {
            return this.getParent().getQuads(state, side, rand);
        }
        ProfileUtil.start("ctm_models");

        IBakedModel parent = this.getParent(rand);
        AbstractCTMBakedModel baked = this;
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        if (Minecraft.getMinecraft().world != null && state instanceof CTMExtendedState ext) {
            ProfileUtil.start("state_creation");
            RenderContextList ctmCtx = ext.getContextList(ext.getClean(), baked);

            Object2LongMap<ICTMTexture<?>> serialized = ctmCtx.serialized();
            ProfileUtil.endAndStart("model_creation");

            // Get cached model specific to this state + layer
            baked = modelcache.get(
                    new State(ext.getClean(), serialized, parent, layer),
                    () -> this.createModel(state, this.model, parent, ctmCtx, rand, layer)
            );
            ProfileUtil.end(); // model_creation
        } else if (state != null) {
            ProfileUtil.start("model_creation");
            // Simple state, but still layer aware
            baked = modelcache.get(
                    new State(state, null, parent, layer),
                    () -> this.createModel(state, this.model, parent, null, rand, layer)
            );
            ProfileUtil.end(); // model_creation
        }

        var quads = baked.quadLookup(side, layer);
        //CTM.logger.info("{}/{}/{}/{}: {}", state, side, layer == null ? "null" : layer.toString().substring(11, 17), baked.layer == null ? "null" : baked.layer.toString().substring(11, 17), quads.size());
        return quads;
    }

    protected final List<BakedQuad> quadLookup(@Nullable EnumFacing side, @Nullable BlockRenderLayer layer) {
        ProfileUtil.start("quad_lookup");
        List<BakedQuad> ret = Collections.emptyList();
        if (layer == this.layer) {
            if (side != null) {
                ret = this.faceQuads.get(side);
            } else {
                ret = this.genQuads;
            }
        }
        ProfileUtil.end(); // quad_lookup
        ProfileUtil.end(); // ctm_models

        if (ret == null) {
            throw new IllegalStateException("getQuads called on a model that was not properly initialized - by using getOverrides and/or getModelData");
        }
        return ret;
    }

    /**
     * Random sensitive parent, will proxy to {@link WeightedBakedModel} if possible.
     */
    @NotNull
    public IBakedModel getParent(long rand) {
        if (this.getParent() instanceof WeightedBakedModel weightedBakedModel) {
            return weightedBakedModel.getRandomModel(rand);
        }
        return this.getParent();
    }

    @NotNull
    public IBakedModel getParent() {
        return this.originalModel;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.overrides;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        IChiselFace face = this.model.getDefaultFace();
        return face != null ? face.getParticle() : super.getParticleTexture();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@NotNull ItemCameraTransforms.TransformType cameraTransformType) {
        return Pair.of(this, super.handlePerspective(cameraTransformType).getRight());
    }

    protected abstract AbstractCTMBakedModel createModel(@Nullable IBlockState state, @NotNull IModelCTM model, IBakedModel parent, @Nullable RenderContextList ctx, long rand, @Nullable BlockRenderLayer layer);

    protected /* abstract */ AbstractCTMBakedModel withNewParent(@NotNull IBakedModel parent) {
        // Pass null for layer as default for items
        return new ModelBakedCTM(this.getModel(), parent, null);
    }

    @Nullable
    private <T> T applyToParent(long rand, Function<AbstractCTMBakedModel, T> func) {
        IBakedModel parent = this.getParent(rand);
        if (parent instanceof AbstractCTMBakedModel ctmBakedModel) {
            return func.apply(ctmBakedModel);
        }
        return null;
    }

    @Nullable
    protected ICTMTexture<?> getOverrideTexture(long rand, int tintIndex, String iconName) {
        ICTMTexture<?> ret = this.getModel().getOverrideTexture(tintIndex, iconName);
        if (ret == null) {
            ret = this.applyToParent(rand, parent -> parent.getOverrideTexture(rand, tintIndex, iconName));
        }
        return ret;
    }

    @Nullable
    protected ICTMTexture<?> getTexture(long rand, String iconName) {
        ICTMTexture<?> ret = this.getModel().getTexture(iconName);
        if (ret == null) {
            ret = this.applyToParent(rand, parent -> parent.getTexture(rand, iconName));
        }
        return ret;
    }

    @Nullable
    protected TextureAtlasSprite getOverrideSprite(long rand, int tintIndex) {
        TextureAtlasSprite ret = this.getModel().getOverrideSprite(tintIndex);
        if (ret == null) {
            ret = this.applyToParent(rand, parent -> parent.getOverrideSprite(rand, tintIndex));
        }
        return ret;
    }

    public Collection<ICTMTexture<?>> getCTMTextures() {
        ImmutableList.Builder<ICTMTexture<?>> builder = ImmutableList.builder();
        builder.addAll(this.getModel().getCTMTextures());
        if (this.getParent() instanceof AbstractCTMBakedModel) {
            builder.addAll(((AbstractCTMBakedModel) this.getParent()).getCTMTextures());
        }
        return builder.build();
    }
}
