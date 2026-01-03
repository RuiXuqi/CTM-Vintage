package team.chisel.ctm.api.model;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface IModelParser {

    @NotNull
    IModelCTM fromJson(ResourceLocation res, JsonObject json);
}
