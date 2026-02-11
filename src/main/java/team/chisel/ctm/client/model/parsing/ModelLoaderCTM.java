package team.chisel.ctm.client.model.parsing;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.model.IModelCTM;
import team.chisel.ctm.api.model.IModelParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum ModelLoaderCTM implements ICustomModelLoader {

    INSTANCE;

    private static final Map<Integer, IModelParser> parserVersions = ImmutableMap.of(1, new ModelParserV1());

    private IResourceManager manager;
    private final Map<ResourceLocation, IModelCTM> loadedModels = Maps.newHashMap();

    private final LoadingCache<ResourceLocation, JsonElement> jsonCache = CacheBuilder.newBuilder().maximumSize(128).build(
            new CacheLoader<>() {
                @Override
                @SuppressWarnings("null")
                public JsonElement load(@NotNull ResourceLocation modelLocation) {
                    String path = modelLocation.getPath() + ".json";
                    if (!path.startsWith("models/")) {
                        path = "models/" + path;
                    }
                    ResourceLocation absolute = new ResourceLocation(modelLocation.getNamespace(), path);

                    try (IResource resource = ModelLoaderCTM.this.manager.getResource(absolute);
                         InputStream resourceInputStream = resource.getInputStream();
                         InputStreamReader resourceInputStreamReader = new InputStreamReader(resourceInputStream)) {
                        JsonElement ele = new JsonParser().parse(resourceInputStreamReader);
                        if (ele != null) {
                            return ele;
                        }
                    } catch (Exception ignored) {
                    }

                    return JsonNull.INSTANCE;
                }
            }
    );

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterModelBaking(ModelBakeEvent event) {
        this.jsonCache.invalidateAll();
    }

    @Override
    public void onResourceManagerReload(@NotNull IResourceManager resourceManager) {
        this.manager = resourceManager;
        this.jsonCache.invalidateAll();
        this.loadedModels.clear();
    }

    @Override
    public boolean accepts(@NotNull ResourceLocation modelLocation) {
        if (modelLocation instanceof ModelResourceLocation) {
            modelLocation = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath());
        }

        JsonElement json = this.jsonCache.getUnchecked(modelLocation);
        return json.isJsonObject() && json.getAsJsonObject().has("ctm_version");
    }

    @Override
    public IModel loadModel(@NotNull ResourceLocation modelLocation) throws IOException {
        this.loadedModels.computeIfAbsent(modelLocation, res -> this.loadFromFile(res, true));
        IModelCTM model = this.loadedModels.get(modelLocation);
        if (model != null) {
            model.load();
        }
        return model;
    }

    public static final Set<ResourceLocation> parsedLocations = new HashSet<>();

    private IModelCTM loadFromFile(ResourceLocation res, boolean forLoad) {
        if (forLoad) {
            parsedLocations.add(new ResourceLocation(res.getNamespace(), res.getPath().replace("models/", "")));
        }

        JsonObject json = this.jsonCache.getUnchecked(res).getAsJsonObject();

        IModelParser parser = parserVersions.get(json.get("ctm_version").getAsInt());
        if (parser == null) {
            throw new IllegalArgumentException("Invalid \"ctm_version\" in model " + res);
        }
        return parser.fromJson(res, json);
    }
}
