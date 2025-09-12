package team.chisel.ctm.client.newctm.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

@Desugar
public record CTMFileDefinition(List<String> logics) {
    public static CTMFileDefinition fromJson(JsonObject json) {
        List<String> logics = new ArrayList<>();
        if (json.has("logics") && json.get("logics").isJsonArray()) {
            JsonArray logicsArray = json.getAsJsonArray("logics");
            for (JsonElement element : logicsArray) {
                logics.add(element.getAsString());
            }
        }
        return new CTMFileDefinition(logics);
    }
}
