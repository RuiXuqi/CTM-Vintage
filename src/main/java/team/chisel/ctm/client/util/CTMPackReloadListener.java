package team.chisel.ctm.client.util;

import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;
import team.chisel.ctm.client.model.parsing.ModelLoaderCTM;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public enum CTMPackReloadListener implements ISelectiveResourceReloadListener {

    INSTANCE;

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, @Nonnull Predicate<IResourceType> resourcePredicate) {
        ResourceUtil.invalidateCaches();
        AbstractCTMBakedModel.invalidateCaches();
        ModelLoaderCTM.parsedLocations.clear();
        TextureMetadataHandler.INSTANCE.invalidateCaches();
    }
}
