package team.chisel.ctm.api.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Deprecated
public interface IChiselFace {

    List<ICTMTexture<?>> getTextureList();

    @NotNull
    TextureAtlasSprite getParticle();
}
