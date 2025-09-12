package team.chisel.ctm.client.newctm.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import org.apache.commons.lang3.tuple.Pair;
import team.chisel.ctm.CTM;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.client.newctm.CTMLogicBakery;
import team.chisel.ctm.client.newctm.ICTMLogic;
import team.chisel.ctm.client.newctm.TextureTypeCustom;
import team.chisel.ctm.client.texture.type.TextureTypeRegistry;
import team.chisel.ctm.client.util.Dir;
import team.chisel.ctm.client.util.Submap;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class CTMDefinitionManager implements ISelectiveResourceReloadListener {

    private final Map<String, ICTMLogic> logicDefinitions = new HashMap<>();
    private final Gson gson = new Gson();

    private static CTMLogicBakery createBakery(CTMLogicDefinition def) {
        var bakery = new CTMLogicBakery();
        var bitNames = new Object2IntOpenHashMap<String>();
        var submapNames = new HashMap<String, Pair<ISubmap, Integer>>();
        var faceNames = new HashMap<String, ISubmap>();
        var bit = def.positions().size() - 1;
        for (var position : def.positions()) {
            bitNames.put(position.id(), bit);
            bakery.input(bit--, Dir.fromDirections(position.directions()));
        }
        var outputId = 0;
        for (var e : def.submaps().entrySet()) {
            for (var p : e.getValue().forName(e.getKey())) {
                submapNames.put(p.getLeft(), Pair.of(p.getRight(), outputId++));
            }
        }
        for (var e : def.faces().entrySet()) {
            for (var p : e.getValue().forName(e.getKey())) {
                faceNames.put(p.getLeft(), p.getRight());
            }
        }
        for (var rule : def.rules()) {
            var submapData = submapNames.get(rule.output());
            var submap = submapData.getLeft();
            var ruleId = submapData.getRight();
            bakery.output(ruleId, rule.from(), submap, rule.at().map(faceNames::get).orElse(Submap.X1));
            for (var connected : rule.connected()) {
                bakery.when(bitNames.getInt(connected), true);
            }
            for (var unconnected : rule.unconnected()) {
                bakery.when(bitNames.getInt(unconnected), false);
            }
        }
        return bakery;
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        if (!resourcePredicate.test(VanillaResourceType.TEXTURES)) return;

        TextureTypeRegistry.lock.writeLock().lock(); // Manually acquire to prevent registry being used in invalid state
        try {
            logicDefinitions.keySet().forEach(TextureTypeRegistry::remove);
            logicDefinitions.clear();

            for (String domain : resourceManager.getResourceDomains()) {
                // Load all ctm.json like sounds.json
                List<String> logics = new ArrayList<>();
                try {
                    for (IResource ctmFile : resourceManager.getAllResources(new ResourceLocation(domain, "ctm.json"))) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ctmFile.getInputStream()))) {
                            JsonObject json = gson.fromJson(reader, JsonObject.class);
                            CTMFileDefinition def = CTMFileDefinition.fromJson(json);
                            logics.addAll(def.logics());
                        } catch (Exception e) {
                            CTM.logger.warn("Invalid ctm.json", e);
                        }
                    }
                } catch (IOException ignored) {
                }

                if (logics.isEmpty()) continue;
                // Load entries collected
                for (String logic : logics) {
                    try {
                        IResource resource = resourceManager.getResource(new ResourceLocation(domain, "ctm_logic/" + logic + ".json"));
                        loadCTMLogicDefinition(resource);
                    } catch (Exception e) {
                        CTM.logger.warn("Failed to read CTM definition: {}", logic, e);
                    }
                }
            }
        } finally {
            TextureTypeRegistry.lock.writeLock().unlock();
        }
    }

    private void loadCTMLogicDefinition(IResource resource) {
        ResourceLocation rl = resource.getResourceLocation();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            CTMLogicDefinition def = CTMLogicDefinition.fromJson(json);
            var bakery = createBakery(def);
            var logic = bakery.bake();
            String id = rl.getNamespace() + ":" + rl.getPath().substring("ctm_logic/".length(), rl.getPath().length() - ".json".length());
            logicDefinitions.put(id, logic);
            TextureTypeRegistry.register(id, new TextureTypeCustom(logic));
        } catch (Exception e) {
            CTM.logger.error("Failed to load CTM definition: {}", rl.toString(), e);
        }
    }
}