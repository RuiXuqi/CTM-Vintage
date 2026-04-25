package team.chisel.ctm.api.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IModel;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.IChiselFace;

import java.util.Collection;

public interface IModelCTM extends IModel {

    IModel getVanillaParent();

    void load();

    // Kept since old versions of Chisel implements it
    Collection<ICTMTexture<?>> getChiselTextures();

    @Nullable
    ICTMTexture<?> getTexture(String iconName);

    boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer);

    @Nullable
    TextureAtlasSprite getOverrideSprite(int tintIndex);

    @Nullable
    ICTMTexture<?> getOverrideTexture(int tintIndex, String sprite);

    /**
     * @deprecated Simply call or implement {@link #getChiselTextures()}. The name is the only difference.
     */
    @Deprecated
    default Collection<ICTMTexture<?>> getCTMTextures() {
        return this.getChiselTextures();
    }

    /**
     * @deprecated Outdated API used by old versions of Chisel
     */
    @SuppressWarnings("unused")
    @Nullable
    @Deprecated
    default IChiselFace getFace(EnumFacing facing) {
        return null;
    }

    /**
     * @deprecated Outdated API used by old versions of Chisel
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Nullable
    @Deprecated
    default IChiselFace getDefaultFace() {
        return null;
    }
}
