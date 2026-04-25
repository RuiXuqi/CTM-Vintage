package team.chisel.ctm.api.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.List;

/**
 * @deprecated Outdated API used by old versions of Chisel
 */
@Deprecated
public interface IChiselFace {

    List<ICTMTexture<?>> getTextureList();

    TextureAtlasSprite getParticle();
}
