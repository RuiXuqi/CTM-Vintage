package team.chisel.ctm.api.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import javax.annotation.Nonnull;
import java.util.List;

@Deprecated
public interface IChiselFace {

    List<ICTMTexture<?>> getTextureList();

    @Nonnull TextureAtlasSprite getParticle();
}