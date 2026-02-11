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

        float uWidth = width * submap.getWidth();
        float vHeight = height * submap.getHeight();
        float xOffset = Quad.lerp(sprite.getMinU(), sprite.getMaxU(), submap.getXOffset());
        float yOffset = Quad.lerp(sprite.getMinV(), sprite.getMaxV(), submap.getYOffset());
        return new PartialTextureAtlasSprite(sprite, xOffset, uWidth, yOffset, vHeight);
    }

    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;

    protected PartialTextureAtlasSprite(TextureAtlasSprite sprite, float xOffset, float uWidth, float yOffset, float vHeight) {
        super(sprite.getIconName());
        this.u0 = xOffset;
        this.u1 = xOffset + uWidth;
        this.v0 = yOffset;
        this.v1 = yOffset + vHeight;

        // Also set width and height to make sure they are correct
        this.width = (int) (sprite.getIconWidth() * (uWidth / (sprite.getMaxU() - sprite.getMinU())));
        this.height = (int) (sprite.getIconHeight() * (vHeight / (sprite.getMaxV() - sprite.getMinV())));
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
        float width = this.getMaxU() - this.getMinU();
        return (float) (this.getMinU() + width * u / 16.0f);
    }

    @Override
    public float getUnInterpolatedU(float offset) {
        float width = this.getMaxU() - this.getMinU();
        return (offset - this.getMinU()) / width * 16.0f;
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
        float height = this.getMaxV() - this.getMinV();
        return (float) (this.getMinV() + height * v / 16.0f);
    }

    @Override
    public float getUnInterpolatedV(float offset) {
        float height = this.getMaxV() - this.getMinV();
        return (offset - this.getMinV()) / height * 16.0f;
    }

    @Override
    public String toString() {
        return "PartialTextureAtlasSprite{contents='" + this.getIconName() + "', u0=" + this.getMinU() + ", u1=" + this.getMaxU() + ", v0=" + this.getMinV() + ", v1=" + this.getMaxV() + "}";
    }
}
