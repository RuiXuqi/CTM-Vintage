package team.chisel.ctm.client.newctm.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Desugar
public record CTMLogicDefinition(List<Position> positions, Map<String, MultiSubmap> submaps, Map<String, MultiSubmap> faces, List<Rule> rules) {
    public static CTMLogicDefinition fromJson(JsonObject json) {
        List<Position> positions = new ArrayList<>();
        JsonArray positionsArray = json.getAsJsonArray("positions");
        for (JsonElement element : positionsArray) {
            positions.add(Position.fromJson(element.getAsJsonObject()));
        }

        Map<String, MultiSubmap> submaps = new HashMap<>();
        JsonObject submapsObj = json.getAsJsonObject("submaps");
        for (Map.Entry<String, JsonElement> entry : submapsObj.entrySet()) {
            submaps.put(entry.getKey(), MultiSubmap.fromJson(entry.getValue().getAsJsonObject()));
        }

        Map<String, MultiSubmap> faces = new HashMap<>();
        if (json.has("faces")) {
            JsonObject facesObj = json.getAsJsonObject("faces");
            for (Map.Entry<String, JsonElement> entry : facesObj.entrySet()) {
                faces.put(entry.getKey(), MultiSubmap.fromJson(entry.getValue().getAsJsonObject()));
            }
        }

        List<Rule> rules = new ArrayList<>();
        JsonArray rulesArray = json.getAsJsonArray("rules");
        for (JsonElement element : rulesArray) {
            rules.add(Rule.fromJson(element.getAsJsonObject()));
        }

        return new CTMLogicDefinition(positions, submaps, faces, rules);
    }
}
