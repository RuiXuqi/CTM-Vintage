package team.chisel.ctm.client.newctm.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Desugar
public record Rule(String output, int from, Optional<String> at, List<String> connected, List<String> unconnected) {
    public static Rule fromJson(JsonObject json) {
        String output = json.get("output").getAsString();
        int from = json.has("from") ? json.get("from").getAsInt() : 0;

        Optional<String> at = Optional.empty();
        if (json.has("at")) {
            at = Optional.of(json.get("at").getAsString());
        }

        List<String> connected = new ArrayList<>();
        if (json.has("connected")) {
            JsonArray connectedArray = json.getAsJsonArray("connected");
            for (JsonElement element : connectedArray) {
                connected.add(element.getAsString());
            }
        }

        List<String> unconnected = new ArrayList<>();
        if (json.has("unconnected")) {
            JsonArray unconnectedArray = json.getAsJsonArray("unconnected");
            for (JsonElement element : unconnectedArray) {
                unconnected.add(element.getAsString());
            }
        }

        return new Rule(output, from, at, connected, unconnected);
    }
}