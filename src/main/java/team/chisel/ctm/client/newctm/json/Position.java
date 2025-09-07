package team.chisel.ctm.client.newctm.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

public final class Position {
    private final String id;
    private final List<EnumFacing> directions;

    public Position(String id, List<EnumFacing> directions) {
        this.id = id;
        this.directions = directions;
    }

    public static Position fromJson(JsonObject json) {
        String id = json.get("id").getAsString();

        List<EnumFacing> directions = new ArrayList<>();
        JsonArray directionsArray = json.getAsJsonArray("directions");
        for (JsonElement element : directionsArray) {
            String facingName = element.getAsString().toUpperCase();
            directions.add(EnumFacing.valueOf(facingName));
        }

        return new Position(id, directions);
    }

    public String id() {
        return id;
    }

    public List<EnumFacing> directions() {
        return directions;
    }
}