package team.chisel.ctm.client.texture.render;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextPillar;
import team.chisel.ctm.client.texture.ctx.TextureContextPillar.ConnectionData;
import team.chisel.ctm.client.texture.ctx.TextureContextPillar.Connections;
import team.chisel.ctm.client.texture.type.TextureTypePillar;
import team.chisel.ctm.client.util.Quad;
import team.chisel.ctm.client.util.Submap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

import static net.minecraft.util.EnumFacing.*;

public class TexturePillar extends AbstractTexture<TextureTypePillar> {

    public TexturePillar(TextureTypePillar type, TextureInfo info) {
        super(type, info);
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad quad, ITextureContext context, int quadGoal) {
        if (context == null) {
            if (quad.getFace() != null && quad.getFace().getAxis().isVertical()) {
                return Lists.newArrayList(makeQuad(quad, context).transformUVs(sprites[0]).rebake());
            }
            return Lists.newArrayList(makeQuad(quad, context).transformUVs(sprites[1], Submap.X2[0][0]).rebake());
        }
        return Lists.newArrayList(getQuad(quad, context));
    }

    @Override
    protected Quad makeQuad(@Nonnull BakedQuad bq, @Nullable ITextureContext context) {
        return super.makeQuad(bq, context).derotate();
    }

    private BakedQuad getQuad(BakedQuad in, ITextureContext context) {
        Quad q = makeQuad(in, context);
        ConnectionData data = ((TextureContextPillar) context).getData();
        Connections cons = data.getConnections();

        // This is the order of operations for connections
        EnumSet<EnumFacing> realConnections = EnumSet.copyOf(data.getConnections().getConnections());
        if (cons.connectedOr(UP, DOWN)) {
            // If connected up or down, ignore all other connections
            realConnections.removeIf(f -> f.getAxis().isHorizontal());
        } else if (cons.connectedOr(EAST, WEST)) {
            // If connected east or west, ignore any north/south connections, and any connections that are already connected up or down
            realConnections.removeIf(f -> f == NORTH || f == SOUTH);
            realConnections.removeIf(f -> blockConnectionZ(f, data));
        } else {
            // Otherwise, remove every connection that is already connected to something else
            realConnections.removeIf(f -> blockConnectionY(f, data));
        }

        // Replace our initial connection data with the new info
        cons = new Connections(realConnections);

        int rotation = 0;
        ISubmap uvs = Submap.X2[0][0];
        if (in.getFace().getAxis().isHorizontal() && cons.connectedOr(UP, DOWN)) {
            uvs = getUVs(UP, DOWN, cons);
        } else if (cons.connectedOr(EAST, WEST)) {
            rotation = 1;
            uvs = getUVs(EAST, WEST, cons);
        } else if (cons.connectedOr(NORTH, SOUTH)) {
            uvs = getUVs(NORTH, SOUTH, cons);
            if (in.getFace() == DOWN) {
                rotation += 2;
            }
        }

        boolean connected = !cons.getConnections().isEmpty();

        // Side textures need to be rotated to look correct
        if (connected && !cons.connectedOr(UP, DOWN)) {
            if (in.getFace() == EAST) {
                rotation += 1;
            }
            if (in.getFace() == NORTH) {
                rotation += 2;
            }
            if (in.getFace() == WEST) {
                rotation += 3;
            }
        }

        // If there is a connection opposite this side, it is an end-cap, so render as unconnected
        if (cons.connected(in.getFace().getOpposite())) {
            connected = false;
        }
        // If there are no connections at all, and this is not the top or bottom, render the "short" column texture
        if (cons.getConnections().isEmpty() && in.getFace().getAxis().isHorizontal()) {
            connected = true;
        }

        q = q.rotate(rotation);
        if (connected) {
            return q.transformUVs(sprites[1], uvs).rebake();
        }
        return q.transformUVs(sprites[0]).rebake();
    }

    private ISubmap getUVs(EnumFacing face1, EnumFacing face2, Connections cons) {
        ISubmap uvs;
        if (cons.connectedAnd(face1, face2)) {
            uvs = Submap.X2[1][0];
        } else {
            if (cons.connected(face1)) {
                uvs = Submap.X2[1][1];
            } else {
                uvs = Submap.X2[0][1];
            }
        }
        return uvs;
    }

    private boolean blockConnectionY(EnumFacing dir, ConnectionData data) {
        return blockConnection(dir, Axis.Y, data) || blockConnection(dir, dir.rotateAround(Axis.Y).getAxis(), data);
    }

    private boolean blockConnectionZ(EnumFacing dir, ConnectionData data) {
        return blockConnection(dir, Axis.Z, data);
    }

    private boolean blockConnection(EnumFacing dir, Axis axis, ConnectionData data) {
        EnumFacing rot = dir.rotateAround(axis);
        return data.getConnections(dir).connectedOr(rot, rot.getOpposite());
    }
}
