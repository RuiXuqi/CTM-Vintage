package team.chisel.ctm.client.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import lombok.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.api.util.NonnullType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@ToString(of = {"vertPos", "vertUv"})
public class Quad {

    @Deprecated
    public static final ISubmap TOP_LEFT = Submap.fromPixelScale(7.8f, 7.8f, 0, 0);
    @Deprecated
    public static final ISubmap TOP_RIGHT = Submap.fromPixelScale(7.8f, 7.8f, 8.2f, 0);
    @Deprecated
    public static final ISubmap BOTTOM_LEFT = Submap.fromPixelScale(7.8f, 7.8f, 0, 8.2f);
    @Deprecated
    public static final ISubmap BOTTOM_RIGHT = Submap.fromPixelScale(7.8f, 7.8f, 8.2f, 8.2f);
    private static final TextureAtlasSprite BASE = null;//Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
    private final Vector3f[] vertPos;
    private final Vector2f[] vertUv;
    private final Builder builder;
    private final int blocklight, skylight;
    // Technically nonfinal, but treated as such except in constructor
    @Getter
    private final UVs uvs;

    private Quad(Vector3f[] verts, Vector2f[] uvs, Builder builder, TextureAtlasSprite sprite) {
        this(verts, uvs, builder, sprite, 0, 0);
    }

    @Deprecated
    private Quad(Vector3f[] verts, Vector2f[] uvs, Builder builder, TextureAtlasSprite sprite, boolean fullbright) {
        this(verts, uvs, builder, sprite, fullbright ? 15 : 0, fullbright ? 15 : 0);
    }

    private Quad(Vector3f[] verts, Vector2f[] uvs, Builder builder, TextureAtlasSprite sprite, int blocklight, int skylight) {
        this.vertPos = verts;
        this.vertUv = uvs;
        this.builder = builder;
        this.uvs = new UVs(sprite, uvs);
        this.blocklight = blocklight;
        this.skylight = skylight;
    }

    @Deprecated
    private Quad(Vector3f[] verts, UVs uvs, Builder builder) {
        this(verts, uvs.vectorize(), builder, uvs.getSprite());
    }

    @Deprecated
    private Quad(Vector3f[] verts, UVs uvs, Builder builder, boolean fullbright) {
        this(verts, uvs.vectorize(), builder, uvs.getSprite(), fullbright);
    }

    private Quad(Vector3f[] verts, UVs uvs, Builder builder, int blocklight, int skylight) {
        this(verts, uvs.vectorize(), builder, uvs.getSprite(), blocklight, skylight);
    }

    public static float lerp(float a, float b, float f) {
        return (a * (1 - f)) + (b * f);
    }

    public static float normalize(float min, float max, float x) {
        if (min == max) return 0.5f;
        return (x - min) / (max - min);
    }

    public static Quad from(BakedQuad baked) {
        Builder b = new Builder(baked.getFormat(), baked.getSprite());
        baked.pipe(b);
        return b.build();
    }

    public Vector3f getVert(int index) {
        return new Vector3f(vertPos[index % 4]);
    }

    public Quad withVert(int index, Vector3f vert) {
        Preconditions.checkElementIndex(index, 4, "Vertex index out of range!");
        Vector3f[] newverts = new Vector3f[4];
        System.arraycopy(vertPos, 0, newverts, 0, newverts.length);
        newverts[index] = vert;
        return new Quad(newverts, getUvs(), builder, blocklight, skylight);
    }

    public Vector2f getUv(int index) {
        return new Vector2f(vertUv[index % 4]);
    }

    public Quad withUv(int index, Vector2f uv) {
        Preconditions.checkElementIndex(index, 4, "UV index out of range!");
        Vector2f[] newuvs = new Vector2f[4];
        System.arraycopy(getUvs().vectorize(), 0, newuvs, 0, newuvs.length);
        newuvs[index] = uv;
        return new Quad(vertPos, new UVs(newuvs), builder, blocklight, skylight);
    }

    public void compute() {

    }

    @Deprecated
    public Quad[] subdivide(int count) {
        if (count == 1) {
            return new Quad[]{this};
        } else if (count != 4) {
            throw new UnsupportedOperationException();
        }

        return subsectAll(Submap.X2);
//        return subsectAll(new ISubmap[] { TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT });
    }

    public Quad[] subsectAll(ISubmap[][] submaps) {
        var stride = submaps[0].length;
        var ret = new Quad[submaps.length * stride];
        for (int i = 0; i < submaps.length; i++) {
            System.arraycopy(subsectAll(submaps[i]), 0, ret, i * stride, stride);
        }
        return ret;
    }

    public Quad[] subsectAll(ISubmap[] submaps) {
        var ret = new Quad[submaps.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = subsect(submaps[i]);
        }
        return ret;
    }

    public Quad subsect(ISubmap submap) {
        submap = submap.unitScale();
        int firstIndex = 0;
        for (int i = 0; i < vertUv.length; i++) {
            if (vertUv[i].y == getUvs().minV && vertUv[i].x == getUvs().minU) {
                firstIndex = i;
                break;
            }
        }

        Vector3f[] positions = new Vector3f[4];
        float[][] uvs = new float[4][];
        for (int i = 0; i < 4; i++) {
            int idx = (firstIndex + i) % 4;
            positions[i] = new Vector3f(vertPos[idx]);
            uvs[i] = new float[]{vertUv[idx].x, vertUv[idx].y};
        }

        var origin = new Vec3d(positions[0].x, positions[0].y, positions[0].z);
        var n1 = new Vec3d(positions[1].x, positions[1].y, positions[1].z).subtract(origin);
        var n2 = new Vec3d(positions[2].x, positions[2].y, positions[2].z).subtract(origin);
        var normalVec = n1.crossProduct(n2).normalize();
        EnumFacing normal = EnumFacing.getFacingFromVector((float) normalVec.x, (float) normalVec.y, (float) normalVec.z);
        TextureAtlasSprite sprite = getUvs().getSprite();
        var xy = new float[4][2];
        var newXy = new float[4][2];
        for (int i = 0; i < 4; i++) {
            switch (normal.getAxis()) {
                case Y:
                    xy[i][0] = positions[i].x;
                    xy[i][1] = positions[i].z;
                    break;
                case Z:
                    xy[i][0] = positions[i].x;
                    xy[i][1] = positions[i].y;
                    break;
                case X:
                    xy[i][0] = positions[i].z;
                    xy[i][1] = positions[i].y;
                    break;
            }
        }

        if (normal.getAxis() == Axis.Y || normal == EnumFacing.SOUTH || normal == EnumFacing.WEST) {
            // Relative X is the same sign for DOWN, UP, SOUTH, and WEST
            newXy[0][0] = Math.max(xy[0][0], submap.getXOffset());                      // DUSW
            newXy[1][0] = Math.max(xy[1][0], submap.getXOffset());                      // DUSW
            newXy[2][0] = Math.min(xy[2][0], submap.getXOffset() + submap.getWidth());  // DUSW
            newXy[3][0] = Math.min(xy[3][0], submap.getXOffset() + submap.getWidth());  // DUSW
        } else {
            // Flip relative X for NORTH and EAST
            newXy[0][0] = Math.min(xy[0][0], submap.getXOffset() + submap.getWidth());  // NE
            newXy[1][0] = Math.min(xy[1][0], submap.getXOffset() + submap.getWidth());  // NE
            newXy[2][0] = Math.max(xy[2][0], submap.getXOffset());                      // NE
            newXy[3][0] = Math.max(xy[3][0], submap.getXOffset());                      // NE
        }

        if (normal != EnumFacing.UP) {
            // Relative Y is the same sign for all but UP
            newXy[0][1] = Math.min(xy[0][1], submap.getYOffset() + submap.getHeight()); // DNSWE
            newXy[1][1] = Math.max(xy[1][1], submap.getYOffset());                      // DNSWE
            newXy[2][1] = Math.max(xy[2][1], submap.getYOffset());                      // DNSWE
            newXy[3][1] = Math.min(xy[3][1], submap.getYOffset() + submap.getHeight()); // DNSWE
        } else {
            // Flip relative Y for UP
            newXy[0][1] = Math.max(xy[0][1], submap.getYOffset());                      // U
            newXy[1][1] = Math.min(xy[1][1], submap.getYOffset() + submap.getHeight()); // U
            newXy[2][1] = Math.min(xy[2][1], submap.getYOffset() + submap.getHeight()); // U
            newXy[3][1] = Math.max(xy[3][1], submap.getYOffset());                      // U
        }

        float u0Interp = normalize(xy[0][0], xy[3][0], newXy[0][0]);
        float v0Interp = normalize(xy[0][1], xy[1][1], newXy[0][1]);
        float u1Interp = normalize(xy[1][0], xy[2][0], newXy[1][0]);
        float v1Interp = normalize(xy[1][1], xy[0][1], newXy[1][1]);
        float u2Interp = normalize(xy[2][0], xy[1][0], newXy[2][0]);
        float v2Interp = normalize(xy[2][1], xy[3][1], newXy[2][1]);
        float u3Interp = normalize(xy[3][0], xy[0][0], newXy[3][0]);
        float v3Interp = normalize(xy[3][1], xy[2][1], newXy[3][1]);

        float u0 = lerp(uvs[0][0], uvs[3][0], u0Interp);
        float v0 = lerp(uvs[0][1], uvs[1][1], v0Interp);
        float u1 = lerp(uvs[1][0], uvs[2][0], u1Interp);
        float v1 = lerp(uvs[1][1], uvs[0][1], v1Interp);
        float u2 = lerp(uvs[2][0], uvs[1][0], u2Interp);
        float v2 = lerp(uvs[2][1], uvs[3][1], v2Interp);
        float u3 = lerp(uvs[3][0], uvs[0][0], u3Interp);
        float v3 = lerp(uvs[3][1], uvs[2][1], v3Interp);

        var newUvs = new UVs(sprite, new Vector2f(u0, v0), new Vector2f(u1, v1), new Vector2f(u2, v2), new Vector2f(u3, v3));

        Vector3f[] newPos = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            newPos[i] = new Vector3f(positions[i]);
            switch (normal.getAxis()) {
                case Y:
                    newPos[i].x = newXy[i][0];
                    newPos[i].z = newXy[i][1];
                    break;
                case Z:
                    newPos[i].x = newXy[i][0];
                    newPos[i].y = newXy[i][1];
                    break;
                case X:
                    newPos[i].z = newXy[i][0];
                    newPos[i].y = newXy[i][1];
                    break;
            }
        }
        return new Quad(newPos, newUvs, builder, blocklight, skylight);
    }

    public Quad rotate(int amount) {
        Vector2f[] uvs = new Vector2f[4];

        TextureAtlasSprite s = getUvs().getSprite();

        for (int i = 0; i < 4; i++) {
            Vector2f normalized = new Vector2f(normalize(s.getMinU(), s.getMaxU(), vertUv[i].x), normalize(s.getMinV(), s.getMaxV(), vertUv[i].y));
            Vector2f uv;
            switch (amount) {
                case 1:
                    uv = new Vector2f(normalized.y, 1 - normalized.x);
                    break;
                case 2:
                    uv = new Vector2f(1 - normalized.x, 1 - normalized.y);
                    break;
                case 3:
                    uv = new Vector2f(1 - normalized.y, normalized.x);
                    break;
                default:
                    uv = new Vector2f(normalized.x, normalized.y);
                    break;
            }
            uvs[i] = uv;
        }

        for (int i = 0; i < uvs.length; i++) {
            uvs[i] = new Vector2f(lerp(s.getMinU(), s.getMaxU(), uvs[i].x), lerp(s.getMinV(), s.getMaxV(), uvs[i].y));
        }

        Quad ret = new Quad(vertPos, uvs, builder, getUvs().getSprite(), blocklight, skylight);
        return ret;
    }

    public Quad derotate() {
        int start = 0;
        for (int i = 0; i < 4; i++) {
            if (vertUv[i].x <= getUvs().minU && vertUv[i].y <= getUvs().minV) {
                start = i;
                break;
            }
        }

        Vector2f[] uvs = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            uvs[i] = vertUv[(i + start) % 4];
        }
        return new Quad(vertPos, uvs, builder, getUvs().getSprite(), blocklight, skylight);
    }

    public Quad setLight(int blocklight, int skylight) {
        return new Quad(this.vertPos, uvs, builder, blocklight, skylight);
    }

    @SuppressWarnings("null")
    public BakedQuad rebake() {
        @Nonnull VertexFormat format = this.builder.vertexFormat;
        // Sorry OF users
        boolean hasLightmap = (this.blocklight > 0 || this.skylight > 0) && !FMLClientHandler.instance().hasOptifine();
        if (hasLightmap) {
            if (format == DefaultVertexFormats.ITEM) { // ITEM is convertable to BLOCK (replace normal+padding with lmap)
                format = DefaultVertexFormats.BLOCK;
            } else if (!format.getElements().contains(DefaultVertexFormats.TEX_2S)) { // Otherwise, this format is unknown, add TEX_2S if it does not exist
                format = new VertexFormat(format).addElement(DefaultVertexFormats.TEX_2S);
            }
        }

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setQuadOrientation(this.builder.quadOrientation);
        builder.setQuadTint(this.builder.quadTint);
        builder.setApplyDiffuseLighting(this.builder.applyDiffuseLighting);
        builder.setTexture(this.uvs.getSprite());

        for (int v = 0; v < 4; v++) {
            for (int i = 0; i < format.getElementCount(); i++) {
                VertexFormatElement ele = format.getElement(i);
                switch (ele.getUsage()) {
                    case UV:
                        if (ele.getIndex() == 1) {
                            //Stuff for fullbright
                            builder.put(i, ((float) blocklight * 0x20) / 0xFFFF, ((float) skylight * 0x20) / 0xFFFF);
                        } else if (ele.getIndex() == 0) {
                            Vector2f uv = vertUv[v];
                            builder.put(i, uv.x, uv.y, 0, 1);
                        }
                        break;
                    case POSITION:
                        Vector3f p = vertPos[v];
                        builder.put(i, p.x, p.y, p.z, 1);
                        break;
                /*case COLOR:
                    builder.put(i, 35, 162, 204); Pretty things
                    break;*/
                    default:
                        builder.put(i, this.builder.data.get(ele.getUsage()).get(v));
                }
            }
        }

        return builder.build();
    }

    public Quad transformUVs(TextureAtlasSprite sprite) {
        return transformUVs(sprite, CTMLogic.FULL_TEXTURE.pixelScale());
    }

    public Quad transformUVs(TextureAtlasSprite sprite, ISubmap submap) {
        return new Quad(vertPos, getUvs().transform(sprite, submap), builder, blocklight, skylight);
    }

    public Quad grow() {
        return new Quad(vertPos, getUvs().normalizeQuadrant(), builder, blocklight, skylight);
    }

    @Deprecated
    public Quad setFullbright(boolean fullbright) {
        if (this.blocklight == 15 != fullbright || this.skylight == 15 != fullbright) {
            return new Quad(vertPos, getUvs(), builder, fullbright);
        } else {
            return this;
        }
    }

    @Value
    public static class Vertex {
        Vector3f pos;
        Vector2f uvs;
    }

    @RequiredArgsConstructor
    public static class Builder implements IVertexConsumer {

        @Getter
        private final VertexFormat vertexFormat;
        @Getter
        private final TextureAtlasSprite sprite;

        @Setter
        private int quadTint = -1;

        @Setter
        private EnumFacing quadOrientation;

        @Setter
        private boolean applyDiffuseLighting;

        private final ListMultimap<EnumUsage, float[]> data = MultimapBuilder.enumKeys(EnumUsage.class).arrayListValues().build();

        @Override
        public void put(int element, @Nullable float... data) {
            if (data == null) return;
            float[] copy = new float[data.length];
            System.arraycopy(data, 0, copy, 0, data.length);
            VertexFormatElement ele = vertexFormat.getElement(element);
            this.data.put(ele.getUsage(), copy);
        }

        public Quad build() {
            Vector3f[] verts = fromData(data.get(EnumUsage.POSITION), 3);
            Vector2f[] uvs = fromData(data.get(EnumUsage.UV), 2);
            return new Quad(verts, uvs, this, getSprite());
        }

        @SuppressWarnings("unchecked")
        private <T extends Vector> T[] fromData(List<float[]> data, int size) {
            Vector[] ret = size == 2 ? new Vector2f[data.size()] : new Vector3f[data.size()];
            for (int i = 0; i < data.size(); i++) {
                ret[i] = size == 2 ? new Vector2f(data.get(i)[0], data.get(i)[1]) : new Vector3f(data.get(i)[0], data.get(i)[1], data.get(i)[2]);
            }
            return (T[]) ret;
        }

        //@Override //soft override, only exists in new forge versions
        public void setTexture(@Nullable TextureAtlasSprite texture) {
        }
    }

    @ToString
    public class UVs implements ISubmap {

        @Getter
        private final TextureAtlasSprite sprite;
        private final Vector2f[] data;
        @Getter
        private final float minU;
        @Getter
        private final float minV;
        @Getter
        private final float maxU;
        @Getter
        private final float maxV;

        private UVs(Vector2f... data) {
            this(BASE, data);
        }

        private UVs(TextureAtlasSprite sprite, Vector2f... data) {
            this.data = data;
            this.sprite = sprite;

            float minU = Float.MAX_VALUE;
            float minV = Float.MAX_VALUE;
            float maxU = 0, maxV = 0;
            for (Vector2f v : data) {
                minU = Math.min(minU, v.x);
                minV = Math.min(minV, v.y);
                maxU = Math.max(maxU, v.x);
                maxV = Math.max(maxV, v.y);
            }
            this.minU = minU;
            this.minV = minV;
            this.maxU = maxU;
            this.maxV = maxV;
        }

        public UVs(float minU, float minV, float maxU, float maxV, TextureAtlasSprite sprite) {
            this.minU = minU;
            this.minV = minV;
            this.maxU = maxU;
            this.maxV = maxV;
            this.sprite = sprite;
            this.data = vectorize();
        }

        public UVs(ISubmap submap, TextureAtlasSprite sprite) {
            this(submap.getXOffset(), submap.getYOffset(), submap.getXOffset() + submap.getWidth(), submap.getYOffset() + submap.getHeight(), sprite);
        }

        public UVs transform(TextureAtlasSprite other, ISubmap submap) {
            UVs normal = normalize();
            submap = submap.unitScale();

            float width = normal.maxU - normal.minU;
            float height = normal.maxV - normal.minV;

            float minU = submap.getXOffset();
            float minV = submap.getYOffset();
            minU += normal.minU * submap.getWidth();
            minV += normal.minV * submap.getHeight();

            float maxU = minU + (width * submap.getWidth());
            float maxV = minV + (height * submap.getHeight());

            // TODO this is horrid
            return new UVs(other,
                    new Vector2f(data[0].x == this.minU ? minU : maxU, data[0].y == this.minV ? minV : maxV),
                    new Vector2f(data[1].x == this.minU ? minU : maxU, data[1].y == this.minV ? minV : maxV),
                    new Vector2f(data[2].x == this.minU ? minU : maxU, data[2].y == this.minV ? minV : maxV),
                    new Vector2f(data[3].x == this.minU ? minU : maxU, data[3].y == this.minV ? minV : maxV))
                    .relativize();
        }

        UVs normalizeQuadrant() {
            UVs normal = normalize();

            int quadrant = normal.getQuadrant();
            float minUInterp = quadrant == 1 || quadrant == 2 ? 0.5f : 0;
            float minVInterp = quadrant < 2 ? 0.5f : 0;
            float maxUInterp = quadrant == 0 || quadrant == 3 ? 0.5f : 1;
            float maxVInterp = quadrant > 1 ? 0.5f : 1;

            normal = new UVs(sprite, normalize(new Vector2f(minUInterp, minVInterp), new Vector2f(maxUInterp, maxVInterp), normal.vectorize()));
            return normal.relativize();
        }

        public UVs normalize() {
            Vector2f min = new Vector2f(sprite.getMinU(), sprite.getMinV());
            Vector2f max = new Vector2f(sprite.getMaxU(), sprite.getMaxV());
            return new UVs(sprite, normalize(min, max, data));
        }

        public UVs relativize() {
            return relativize(sprite);
        }

        public UVs relativize(TextureAtlasSprite sprite) {
            Vector2f min = new Vector2f(sprite.getMinU(), sprite.getMinV());
            Vector2f max = new Vector2f(sprite.getMaxU(), sprite.getMaxV());
            return new UVs(sprite, lerp(min, max, data));
        }

        @SuppressWarnings("null")
        public Vector2f[] vectorize() {
            return data == null ? new Vector2f[]{new Vector2f(minU, minV), new Vector2f(minU, maxV), new Vector2f(maxU, maxV), new Vector2f(maxU, minV)} : data;
        }

        private Vector2f[] normalize(Vector2f min, Vector2f max, @NonnullType Vector2f... vecs) {
            Vector2f[] ret = new Vector2f[vecs.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = normalize(min, max, vecs[i]);
            }
            return ret;
        }

        private Vector2f normalize(Vector2f min, Vector2f max, Vector2f vec) {
            return new Vector2f(Quad.normalize(min.x, max.x, vec.x), Quad.normalize(min.y, max.y, vec.y));
        }

        private Vector2f[] lerp(Vector2f min, Vector2f max, @NonnullType Vector2f... vecs) {
            Vector2f[] ret = new Vector2f[vecs.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = lerp(min, max, vecs[i]);
            }
            return ret;
        }

        private Vector2f lerp(Vector2f min, Vector2f max, Vector2f vec) {
            return new Vector2f(Quad.lerp(min.x, max.x, vec.x), Quad.lerp(min.y, max.y, vec.y));
        }

        public int getQuadrant() {
            if (maxU <= 0.5f) {
                if (maxV <= 0.5f) {
                    return 3;
                } else {
                    return 0;
                }
            } else {
                if (maxV <= 0.5f) {
                    return 2;
                } else {
                    return 1;
                }
            }
        }

        @Override
        public float getYOffset() {
            return minV;
        }

        @Override
        public float getXOffset() {
            return minU;
        }

        @Override
        public float getWidth() {
            return maxU - minU;
        }

        @Override
        public float getHeight() {
            return maxV - minV;
        }

        @Override
        public ISubmap unitScale() {
            return this;
        }

        @Override
        public ISubmap pixelScale() {
            return new SubmapRescaled(this, PIXELS_PER_UNIT, true);
        }
    }
}
