package team.chisel.ctm.client.model;

import com.google.common.collect.*;
import com.google.gson.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.IClip;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.model.IModelCTM;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.IMetadataSectionCTM;
import team.chisel.ctm.client.texture.render.TextureNormal;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;
import team.chisel.ctm.client.util.ResourceUtil;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModelCTM implements IModelCTM {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(IMetadataSectionCTM.class, new IMetadataSectionCTM.Serializer()).create();

    // Legacy reflection handle for vanilla model compatibility in 1.12.2
    private static final MethodHandle _asVanillaModel;

    static {
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().unreflect(IModel.class.getMethod("asVanillaModel"));
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException e) {
            mh = null;
        }
        _asVanillaModel = mh;
    }

    private final ModelBlock modelinfo;
    private final IModel vanillamodel;
    private Boolean uvlock;

    // Populated from overrides data during construction
    private final Int2ObjectMap<JsonElement> overrides;
    protected final Int2ObjectMap<IMetadataSectionCTM> metaOverrides = new Int2ObjectArrayMap<>();

    // Populated during bake with real texture data
    protected Int2ObjectMap<TextureAtlasSprite> spriteOverrides;
    protected Map<Pair<Integer, ResourceLocation>, ICTMTexture<?>> textureOverrides;

    private final Collection<ResourceLocation> textureDependencies;

    private final EnumSet<BlockRenderLayer> extraLayers = EnumSet.noneOf(BlockRenderLayer.class);

    private final Map<String, ICTMTexture<?>> textures = new HashMap<>();

    public ModelCTM(ModelBlock modelinfo, IModel vanillamodel, Int2ObjectMap<JsonElement> overrides) throws IOException {
        this.modelinfo = modelinfo;
        this.vanillamodel = vanillamodel;
        this.overrides = overrides;

        this.textureDependencies = new HashSet<>();
        this.textureDependencies.addAll(vanillamodel.getTextures());
        for (Entry<Integer, JsonElement> e : this.overrides.entrySet()) {
            IMetadataSectionCTM meta = null;
            if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
                ResourceLocation rl = new ResourceLocation(e.getValue().getAsString());
                meta = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(rl));
                textureDependencies.add(rl);
            } else if (e.getValue().isJsonObject()) {
                JsonObject obj = e.getValue().getAsJsonObject();
                if (!obj.has("ctm_version")) {
                    // This model can only be version 1, TODO improve this
                    obj.addProperty("ctm_version", 1);
                }
                // Compatibility for object-style definition
                if (obj.has("texture")) {
                    ResourceLocation rl = new ResourceLocation(obj.get("texture").getAsString());
                    textureDependencies.add(rl);
                }
                meta = GSON.fromJson(obj, IMetadataSectionCTM.class);
            }
            if (meta != null) {
                metaOverrides.put(e.getKey(), meta);
                textureDependencies.addAll(Arrays.asList(meta.getAdditionalTextures()));
            }
        }

        this.textureDependencies.removeIf(rl -> rl.getPath().startsWith("#"));

        // Validate all texture metadata
        for (ResourceLocation res : getTextures()) {
            IMetadataSectionCTM meta = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(res));
            if (meta != null && meta.getType().requiredTextures() != meta.getAdditionalTextures().length + 1) {
                throw new IOException(String.format("Texture type %s requires exactly %d textures. %d were provided.", meta.getType(), meta.getType().requiredTextures(), meta.getAdditionalTextures().length + 1));
            }
        }
    }

    @Override
    public IModel getVanillaParent() {
        return vanillamodel;
    }

    // Soft override for IModel compatibility
    @SuppressWarnings("unchecked")
    public Optional<ModelBlock> asVanillaModel() {
        return Optional.ofNullable(_asVanillaModel)
                .<Optional<ModelBlock>>map(mh -> {
                    try {
                        return (Optional<ModelBlock>) mh.invokeExact(getVanillaParent());
                    } catch (Throwable e1) {
                        return Optional.empty();
                    }
                })
                .filter(Optional::isPresent)
                .orElse(Optional.ofNullable(modelinfo));
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return textureDependencies;
    }

    @Override
    public IBakedModel bake(@NotNull IModelState state, @NotNull VertexFormat format, @NotNull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        // Initialize textures from vanilla model bake
        IBakedModel parent = vanillamodel.bake(state, format, rl -> initializeTexture(rl, bakedTextureGetter));

        // Initialize CTM overrides (Lazy init pattern from new version)
        if (!isInitialized()) {
            initializeOverrides(bakedTextureGetter);
        }

        return new ModelBakedCTM(this, parent, null);
    }

    public TextureAtlasSprite initializeTexture(ResourceLocation rl, Function<ResourceLocation, TextureAtlasSprite> spriteGetter) {
        TextureAtlasSprite sprite = spriteGetter.apply(rl);
        IMetadataSectionCTM chiselmeta = null;
        try {
            chiselmeta = ResourceUtil.getMetadata(sprite);
        } catch (IOException ignored) {
        }
        final IMetadataSectionCTM meta = chiselmeta;
        textures.computeIfAbsent(sprite.getIconName(), s -> {
            ICTMTexture<?> tex;
            if (meta == null) {
                tex = new TextureNormal(TextureTypeNormal.INSTANCE, new TextureInfo(new TextureAtlasSprite[]{sprite}, Optional.empty(), null, false));
            } else {
                tex = meta.makeTexture(sprite, spriteGetter);
            }
            BlockRenderLayer renderLayer = tex.getLayer();
            if (renderLayer != null) {
                extraLayers.add(renderLayer);
            }
            return tex;
        });
        return sprite;
    }

    private void initializeOverrides(Function<ResourceLocation, TextureAtlasSprite> spriteGetter) {
        if (spriteOverrides == null) {
            spriteOverrides = new Int2ObjectOpenHashMap<>();
            // Convert all primitive values into sprites
            for (Entry<Integer, JsonElement> e : overrides.entrySet()) {
                if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
                    TextureAtlasSprite sprite = spriteGetter.apply(new ResourceLocation(e.getValue().getAsString()));
                    spriteOverrides.put(e.getKey(), sprite);
                } else if (e.getValue().isJsonObject()) {
                    JsonElement texture = e.getValue().getAsJsonObject().get("texture");
                    if (texture != null && texture.isJsonPrimitive()) {
                        spriteOverrides.put(e.getKey(), spriteGetter.apply(new ResourceLocation(texture.getAsString())));
                    }
                }
            }
        }
        if (textureOverrides == null) {
            textureOverrides = new HashMap<>();
            for (Entry<Integer, IMetadataSectionCTM> e : metaOverrides.entrySet()) {
                // Find faces matching the tint index
                List<BlockPartFace> matches = modelinfo.getElements().stream()
                        .flatMap(b -> b.mapFaces.values().stream())
                        .filter(b -> b.tintIndex == e.getKey())
                        .collect(Collectors.toList());

                Multimap<String, BlockPartFace> bySprite = HashMultimap.create();
                // Map texture variables to actual locations
                matches.forEach(part -> bySprite.put(modelinfo.textures.getOrDefault(part.texture.substring(1), part.texture), part));

                for (Entry<String, Collection<BlockPartFace>> e2 : bySprite.asMap().entrySet()) {
                    ResourceLocation texLoc = new ResourceLocation(e2.getKey());
                    TextureAtlasSprite sprite = getOverrideSprite(e.getKey());
                    if (sprite == null) sprite = spriteGetter.apply(texLoc);
                    ICTMTexture<?> tex = e.getValue().makeTexture(sprite, spriteGetter);
                    BlockRenderLayer layer = tex.getLayer();
                    if (layer != null) extraLayers.add(layer);
                    textureOverrides.put(Pair.of(e.getKey(), texLoc), tex);
                }
            }
        }
    }

    public boolean isInitialized() {
        return spriteOverrides != null && textureOverrides != null && !textures.isEmpty();
    }

    @Override
    public IModelState getDefaultState() {
        return getVanillaParent().getDefaultState();
    }

    public Optional<? extends IClip> getClip(@NotNull String name) {
        return getVanillaParent().getClip(name);
    }

    @Override
    public void load() {
    }

    @Override
    public Collection<ICTMTexture<?>> getChiselTextures() {
        return ImmutableList.<ICTMTexture<?>>builder().addAll(textures.values()).addAll(textureOverrides.values()).build();
    }

    @Override
    public ICTMTexture<?> getTexture(String iconName) {
        return textures.get(iconName);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        // Check if the layer is in the enum set, or if it matches the block's default layer.
        // If extraLayers is empty, it means no CTM texture requested a specific layer, so we default to the block's layer.
        return extraLayers.contains(layer) || state.getBlock().getRenderLayer() == layer;
    }

    @Override
    @Nullable
    public TextureAtlasSprite getOverrideSprite(int tintIndex) {
        return spriteOverrides.get(tintIndex);
    }

    @Override
    @Nullable
    public ICTMTexture<?> getOverrideTexture(int tintIndex, String sprite) {
        return textureOverrides.get(Pair.of(tintIndex, sprite));
    }

    @Override
    public IModel retexture(@NotNull ImmutableMap<String, String> textures) {
        try {
            ModelCTM ret = deepCopy(getVanillaParent().retexture(textures), null, null);

            ret.modelinfo.textures.putAll(textures);
            for (Entry<Integer, IMetadataSectionCTM> e : ret.metaOverrides.entrySet()) {
                ResourceLocation[] additionals = e.getValue().getAdditionalTextures();
                for (int i = 0; i < additionals.length; i++) {
                    ResourceLocation res = additionals[i];
                    if (res.getPath().startsWith("#")) {
                        String newTexture = textures.get(res.getPath().substring(1));
                        if (newTexture != null) {
                            additionals[i] = new ResourceLocation(newTexture);
                            ret.textureDependencies.add(additionals[i]);
                        }
                    }
                }
            }
            // Update overrides to point to new textures
            for (int i : ret.overrides.keySet()) {
                ret.overrides.computeIfPresent(i, (idx, ele) -> {
                    if (ele.isJsonPrimitive() && ele.getAsJsonPrimitive().isString()) {
                        String val = ele.getAsString();
                        if (val.startsWith("#")) {
                            String newTexture = textures.get(val.substring(1));
                            if (newTexture != null) {
                                ele = new JsonPrimitive(newTexture);
                                ret.textureDependencies.add(new ResourceLocation(ele.getAsString()));
                            }
                        }
                    }
                    return ele;
                });
            }
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return ModelLoaderRegistry.getMissingModel();
        }
    }

    @Override
    public IModel uvlock(boolean value) {
        if (uvlock == null || uvlock != value) {
            IModel newParent = getVanillaParent().uvlock(value);
            if (newParent != getVanillaParent()) {
                IModel ret = deepCopyOrMissing(newParent, null, null);
                if (ret instanceof ModelCTM) {
                    ((ModelCTM) ret).uvlock = value;
                }
                return ret;
            }
        }
        return this;
    }

    /**
     * Allows the model to process custom data from the variant definition.
     * If unknown data is encountered it should be skipped.
     *
     * @return a new model, with data applied.
     */
    @Override
    public IModel process(@NotNull ImmutableMap<String, String> customData) {
        return deepCopyOrMissing(getVanillaParent().process(customData), null, null);
    }

    @Override
    public IModel smoothLighting(boolean value) {
        if (modelinfo.isAmbientOcclusion() != value) {
            return deepCopyOrMissing(getVanillaParent().smoothLighting(value), value, null);
        }
        return this;
    }

    @Override
    public IModel gui3d(boolean value) {
        if (modelinfo.isGui3d() != value) {
            return deepCopyOrMissing(getVanillaParent().gui3d(value), null, value);
        }
        return this;
    }

    private IModel deepCopyOrMissing(IModel newParent, Boolean ao, Boolean gui3d) {
        try {
            return deepCopy(newParent, ao, gui3d);
        } catch (IOException e) {
            e.printStackTrace();
            return ModelLoaderRegistry.getMissingModel();
        }
    }

    private ModelCTM deepCopy(IModel newParent, Boolean ao, Boolean gui3d) throws IOException {
        // Deep copy logic taken from ModelLoader$VanillaModelWrapper
        List<BlockPart> parts = new ArrayList<>();
        for (BlockPart part : modelinfo.getElements()) {
            parts.add(new BlockPart(part.positionFrom, part.positionTo, Maps.newHashMap(part.mapFaces), part.partRotation, part.shade));
        }

        ModelBlock newModel = new ModelBlock(modelinfo.getParentLocation(), parts,
                Maps.newHashMap(modelinfo.textures), ao == null ? modelinfo.isAmbientOcclusion() : ao, gui3d == null ? modelinfo.isGui3d() : gui3d,
                modelinfo.getAllTransforms(), Lists.newArrayList(modelinfo.getOverrides()));

        newModel.name = modelinfo.name;
        newModel.parent = modelinfo.parent;
        return new ModelCTM(newModel, newParent, new Int2ObjectArrayMap<>(overrides));
    }
}
