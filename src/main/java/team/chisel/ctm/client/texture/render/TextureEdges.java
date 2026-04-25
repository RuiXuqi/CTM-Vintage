package team.chisel.ctm.client.texture.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.Configurations;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextCTM;
import team.chisel.ctm.client.texture.type.TextureTypeEdges;
import team.chisel.ctm.client.texture.type.TextureTypeEdges.CTMLogicEdges;
import team.chisel.ctm.client.util.Quad;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TextureEdges extends TextureCTM<TextureTypeEdges> {

    public TextureEdges(TextureTypeEdges type, TextureInfo info) {
        super(type, info);
    }

    @Override
    public List<BakedQuad> transformQuad(@NotNull BakedQuad bq, @Nullable ITextureContext context, int quadGoal) {
        Quad quad = this.makeQuad(bq, context);
        if (context == null || Configurations.disableCTM) {
            return Collections.singletonList(quad.transformUVs(this.sprites[0]).rebake());
        }

        CTMLogicEdges logic = (CTMLogicEdges) ((TextureContextCTM) context).getCTM(bq.getFace());
        if (logic.isObscured()) {
            return Arrays.stream(quad.transformUVs(this.sprites[2]).subdivide(4)).filter(Objects::nonNull).map(Quad::rebake).collect(Collectors.toList());
        }

        return super.transformQuad(bq, context, quadGoal);
    }
}
