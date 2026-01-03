package team.chisel.ctm.client.newctm.json;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

@Desugar
public record Position(String id, List<EnumFacing> directions) {
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
}
