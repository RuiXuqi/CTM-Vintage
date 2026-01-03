package team.chisel.ctm.client.util;

import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;

import java.util.function.Predicate;

public enum CTMPackReloadListener implements ISelectiveResourceReloadListener {
    INSTANCE;

    @Override
    public void onResourceManagerReload(@NotNull IResourceManager resourceManager, @NotNull Predicate<IResourceType> resourcePredicate) {
        if (!resourcePredicate.test(VanillaResourceType.TEXTURES)) return;

        ResourceUtil.invalidateCaches();
        TextureMetadataHandler.INSTANCE.invalidateCaches();
        AbstractCTMBakedModel.invalidateCaches();
    }
}
