package team.chisel.ctm.client.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Optional;

public class ParseUtils {

    public static Optional<Boolean> getBoolean(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            return Optional.of(element.getAsBoolean());
        }
        return Optional.empty();
    }

    public static Optional<Boolean> getBoolean(JsonObject object, String memberName) {
        if (object.has(memberName)) {
            return getBoolean(object.get(memberName));
        }
        return Optional.empty();
    }
}
