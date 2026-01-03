package team.chisel.ctm.client.texture.type;

import lombok.RequiredArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextPosition;
import team.chisel.ctm.client.texture.render.TextureMap;
import team.chisel.ctm.client.texture.render.TextureMap.MapType;


@RequiredArgsConstructor
public class TextureTypeMap implements ITextureType {

    private final MapType type;

    @Override
    public TextureMap makeTexture(@NotNull TextureInfo info) {
        return new TextureMap(this, info, type);
    }

    @Override
    public ITextureContext getBlockRenderContext(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull ICTMTexture<?> tex) {
        return type.getContext(world, pos, (TextureMap) tex);
    }

    @Override
    public ITextureContext getContextFromData(long data) {
        return new TextureContextPosition(BlockPos.fromLong(data));
    }

    @TextureType("r")
    @TextureType("random")
    public static final TextureTypeMap R = new TextureTypeMap(MapType.RANDOM);

    @TextureType("v")
    @TextureType("pattern")
    public static final TextureTypeMap V = new TextureTypeMap(MapType.PATTERNED);
}
