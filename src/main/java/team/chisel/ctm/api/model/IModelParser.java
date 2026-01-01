package team.chisel.ctm.api.model;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public interface IModelParser {

    @Nonnull
    IModelCTM fromJson(ResourceLocation res, JsonObject json);
}
