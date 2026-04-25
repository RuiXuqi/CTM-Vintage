package team.chisel.ctm.api.model;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public interface IModelParser {

    IModelCTM fromJson(ResourceLocation res, JsonObject json);
}
