package team.chisel.ctm.client.util;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.chisel.ctm.api.texture.ISubmap;

public class PartialTextureAtlasSprite extends TextureAtlasSprite {

    public static TextureAtlasSprite createPartial(TextureAtlasSprite sprite, ISubmap submap) {
        submap = submap.unitScale();
        if (submap.getXOffset() == 0 && submap.getYOffset() == 0 && submap.getWidth() == 1 && submap.getHeight() == 1) {
            return sprite;
        }

        float width = sprite.getMaxU() - sprite.getMinU();
        float height = sprite.getMaxV() - sprite.getMinV();

        float atlasWidth = sprite.getIconWidth() / width;
        float atlasHeight = sprite.getIconHeight() / height;

        float uWidth = width * submap.getWidth();
        float vHeight = height * submap.getHeight();
        float xOffset = Quad.lerp(sprite.getMinU(), sprite.getMaxU(), submap.getXOffset());
        float yOffset = Quad.lerp(sprite.getMinV(), sprite.getMaxV(), submap.getYOffset());
        return new PartialTextureAtlasSprite(sprite, atlasWidth, atlasHeight, xOffset, uWidth, yOffset, vHeight);
    }

    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;

    protected PartialTextureAtlasSprite(TextureAtlasSprite sprite, float atlasWidth, float atlasHeight, float xOffset, float uWidth, float yOffset, float vHeight) {
        super(sprite.getIconName());
        this.u0 = xOffset;
        this.u1 = xOffset + uWidth;
        this.v0 = yOffset;
        this.v1 = yOffset + vHeight;

        this.width = (int) atlasWidth;
        this.height = (int) atlasHeight;
    }

    @Override
    public float getMinU() {
        return this.u0;
    }

    @Override
    public float getMaxU() {
        return this.u1;
    }

    @Override
    public float getInterpolatedU(double u) {
        float width = getMaxU() - getMinU();
        return (float) (getMinU() + width * u / 16.0f);
    }

    @Override
    public float getUnInterpolatedU(float offset) {
        float width = getMaxU() - getMinU();
        return (offset - getMinU()) / width * 16.0f;
    }

    @Override
    public float getMinV() {
        return this.v0;
    }

    @Override
    public float getMaxV() {
        return this.v1;
    }

    @Override
    public float getInterpolatedV(double v) {
        float height = getMaxV() - getMinV();
        return (float) (getMinV() + height * v / 16.0f);
    }

    @Override
    public float getUnInterpolatedV(float offset) {
        float height = getMaxV() - getMinV();
        return (offset - getMinV()) / height * 16.0f;
    }

    @Override
    public String toString() {
        return "PartialTextureAtlasSprite{contents='" + getIconName() + "', u0=" + getMinU() + ", u1=" + getMaxU() + ", v0=" + getMinV() + ", v1=" + getMaxV() + "}";
    }

/*    private float atlasSize() {
        float atlasWidth = getIconWidth() / (getMaxU() - getMinU());
        float atlasHeight = getIconHeight() / (getMaxV() - getMinV());
        return Math.max(atlasWidth, atlasHeight);
    }

    @Override
    public float uvShrinkRatio() {
        return 4.0F / atlasSize();
    }*/
}
