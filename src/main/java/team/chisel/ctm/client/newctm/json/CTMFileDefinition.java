package team.chisel.ctm.client.newctm.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class CTMFileDefinition {
    private final List<String> logics;

    public CTMFileDefinition(List<String> logics) {
        this.logics = logics;
    }

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

    public List<String> logics() {
        return logics;
    }
}