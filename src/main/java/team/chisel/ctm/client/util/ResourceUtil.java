package team.chisel.ctm.client.util;

import com.google.gson.JsonParseException;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import team.chisel.ctm.client.texture.IMetadataSectionCTM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class ResourceUtil {

    public static ResourceLocation toResourceLocation(TextureAtlasSprite sprite) {
        return new ResourceLocation(sprite.getIconName());
    }

    public static IResource getResource(TextureAtlasSprite sprite) throws IOException {
        return getResource(spriteToAbsolute(toResourceLocation(sprite)));
    }

    public static ResourceLocation spriteToAbsolute(ResourceLocation sprite) {
        if (!sprite.getPath().startsWith("textures/")) {
            sprite = new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath());
        }
        if (!sprite.getPath().endsWith(".png")) {
            sprite = new ResourceLocation(sprite.getNamespace(), sprite.getPath() + ".png");
        }
        return sprite;
    }

    public static IResource getResource(ResourceLocation res) throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(res);
    }

    public static IResource getResourceUnsafe(ResourceLocation res) {
        try {
            return getResource(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<ResourceLocation, IMetadataSectionCTM> metadataCache = new HashMap<>();

    public static Optional<IMetadataSectionCTM> getMetadata(ResourceLocation res) throws IOException {
        // Note, semantically different from computeIfAbsent, as we DO care about keys mapped to null values
        if (metadataCache.containsKey(res)) {
            return Optional.ofNullable(metadataCache.get(res));
        }
        Optional<IMetadataSectionCTM> ret;
        try {
            IResource resource = getResource(res);
            ret = Optional.ofNullable(resource.getMetadata(IMetadataSectionCTM.SECTION_NAME));
        } catch (FileNotFoundException e) {
            ret = Optional.empty();
        } catch (JsonParseException e) {
            throw new IOException("Error loading metadata for location " + res, e);
        }
        metadataCache.put(res, ret.orElse(null));
        return ret;
    }

    public static Optional<IMetadataSectionCTM> getMetadata(TextureAtlasSprite sprite) throws IOException {
        return getMetadata(spriteToAbsolute(toResourceLocation(sprite)));
    }

    public static Optional<IMetadataSectionCTM> getMetadataUnsafe(TextureAtlasSprite sprite) {
        try {
            return getMetadata(sprite);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void invalidateCaches() {
        metadataCache.clear();
    }
}
