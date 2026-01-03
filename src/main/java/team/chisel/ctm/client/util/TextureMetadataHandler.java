package team.chisel.ctm.client.util;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.CTM;
import team.chisel.ctm.api.event.TextureCollectedEvent;
import team.chisel.ctm.api.model.IModelCTM;
import team.chisel.ctm.client.model.ModelBakedCTM;
import team.chisel.ctm.client.model.ModelCTM;
import team.chisel.ctm.client.model.parsing.ModelLoaderCTM;
import team.chisel.ctm.client.texture.IMetadataSectionCTM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public enum TextureMetadataHandler {

    INSTANCE;

    private final Set<ResourceLocation> registeredTextures = new HashSet<>();
    private final Object2BooleanMap<ResourceLocation> wrappedModels = new Object2BooleanLinkedOpenHashMap<>();

    /*
     * Handle stitching metadata additional textures
     */
    @SubscribeEvent
    public void onTextureStitch(TextureCollectedEvent event) {
        TextureAtlasSprite sprite = event.getSprite();
        try {
            ResourceLocation rel = new ResourceLocation(sprite.getIconName());
            rel = new ResourceLocation(rel.getNamespace(), "textures/" + rel.getPath() + ".png");
            Optional<IMetadataSectionCTM> metadata = ResourceUtil.getMetadata(rel);
            var proxy = metadata.map(IMetadataSectionCTM::getProxy);
            if (proxy.isPresent()) {
                ResourceLocation proxysprite = new ResourceLocation(proxy.get());
                Optional<IMetadataSectionCTM> proxymeta = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(proxysprite));
                // Load proxy's base sprite
                event.getMap().registerSprite(proxysprite);
                proxymeta.ifPresent(m -> {
                    // Load proxy's additional textures
                    for (ResourceLocation r : m.getAdditionalTextures()) {
                        if (registeredTextures.add(r)) {
                            event.getMap().registerSprite(r);
                        }
                    }
                });
            }
            metadata.map(IMetadataSectionCTM::getAdditionalTextures)
                    .ifPresent(textures -> {
                        // Load additional textures
                        for (ResourceLocation r : textures) {
                            if (registeredTextures.add(r)) {
                                event.getMap().registerSprite(r);
                            }
                        }
                    });
        } catch (FileNotFoundException ignored) { // Ignore these, they are reported by vanilla
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Handle wrapping models that use CTM textures
     */

    private static final Class<?> multipartModelClass;
    private static final Class<?> vanillaModelWrapperClass;
    private static final Field multipartPartModels;
    private static final Field modelWrapperModel;

    static {
        try {
            multipartModelClass = Class.forName("net.minecraftforge.client.model.ModelLoader$MultipartModel");
            multipartPartModels = multipartModelClass.getDeclaredField("partModels");
            multipartPartModels.setAccessible(true);
            vanillaModelWrapperClass = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper");
            modelWrapperModel = vanillaModelWrapperClass.getDeclaredField("model");
            modelWrapperModel.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException e) {
            throw Throwables.propagate(e);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST) // low priority to capture all event-registered models
    @SneakyThrows
    public void onModelBake(ModelBakeEvent event) {
        Map<ModelResourceLocation, IModel> stateModels = ReflectionHelper.getPrivateValue(ModelLoader.class, event.getModelLoader(), "stateModels");
        for (ModelResourceLocation mrl : event.getModelRegistry().getKeys()) {
            IModel rootModel = stateModels.get(mrl);
            if (rootModel != null && !(rootModel instanceof IModelCTM) && !ModelLoaderCTM.parsedLocations.contains(mrl)) {
                Deque<ResourceLocation> dependencies = new ArrayDeque<>();
                Set<ResourceLocation> seenModels = new HashSet<>();
                dependencies.push(mrl);
                seenModels.add(mrl);
                boolean shouldWrap = wrappedModels.getOrDefault(mrl, false);
                // Breadth-first loop through dependencies, exiting as soon as a CTM texture is found, and skipping duplicates/cycles
                while (!shouldWrap && !dependencies.isEmpty()) {
                    ResourceLocation dep = dependencies.pop();
                    IModel model;
                    try {
                        model = dep == mrl ? rootModel : ModelLoaderRegistry.getModel(dep);
                    } catch (Exception e) {
                        continue;
                    }

                    Set<ResourceLocation> textures = Sets.newHashSet(model.getTextures());
                    // FORGE WHY
                    if (vanillaModelWrapperClass.isAssignableFrom(model.getClass())) {
                        ModelBlock parent = ((ModelBlock) modelWrapperModel.get(model)).parent;
                        while (parent != null) {
                            textures.addAll(parent.textures.values().stream().filter(s -> !s.startsWith("#")).map(ResourceLocation::new).collect(Collectors.toSet()));
                            parent = parent.parent;
                        }
                    }

                    Set<ResourceLocation> newDependencies = Sets.newHashSet(model.getDependencies());

                    // FORGE WHYYYYY
                    if (multipartModelClass.isAssignableFrom(model.getClass())) {
                        Map<?, IModel> partModels = (Map<?, IModel>) multipartPartModels.get(model);
                        textures = partModels.values().stream().map(IModel::getTextures).flatMap(Collection::stream).collect(Collectors.toSet());
                        newDependencies.addAll(partModels.values().stream().flatMap(m -> m.getDependencies().stream()).collect(Collectors.toList()));
                    }

                    for (ResourceLocation tex : textures) {
                        Optional<IMetadataSectionCTM> meta = Optional.empty();
                        try {
                            meta = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(tex));
                        } catch (IOException ignored) {
                        } // Fallthrough
                        if (meta.isPresent()) {
                            shouldWrap = true;
                            break;
                        }
                    }

                    for (ResourceLocation rl : newDependencies) {
                        if (seenModels.add(rl)) {
                            dependencies.push(rl);
                        }
                    }
                }
                wrappedModels.put(mrl, shouldWrap);
                if (shouldWrap) {
                    try {
                        event.getModelRegistry().putObject(mrl, wrap(rootModel, event.getModelRegistry().getObject(mrl)));
                        dependencies.clear();
                    } catch (IOException e) {
                        CTM.logger.error("Could not wrap model " + mrl + ". Aborting...", e);
                    }
                }
            }
        }
    }

    private @NotNull IBakedModel wrap(IModel model, IBakedModel object) throws IOException {
        ModelCTM modelchisel = new ModelCTM(null, model, Int2ObjectMaps.emptyMap());
        modelchisel.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, rl -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString()));
        return new ModelBakedCTM(modelchisel, object, null);
    }

    public void invalidateCaches() {
        registeredTextures.clear();
        wrappedModels.clear();
    }
}
