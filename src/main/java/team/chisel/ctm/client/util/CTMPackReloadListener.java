package team.chisel.ctm.client.util;

import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;

import java.util.function.Predicate;

public class CTMPackReloadListener implements ISelectiveResourceReloadListener {
    public static final CTMPackReloadListener INSTANCE = new CTMPackReloadListener();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        if (!resourcePredicate.test(VanillaResourceType.TEXTURES)) return;

        ResourceUtil.invalidateCaches();
        TextureMetadataHandler.INSTANCE.invalidateCaches();
        AbstractCTMBakedModel.invalidateCaches();
    }
}
