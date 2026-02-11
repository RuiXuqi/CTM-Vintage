package team.chisel.ctm.api.texture;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.chisel.ctm.client.util.Submap;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface ISubmap {

    float getYOffset();

    float getXOffset();

    float getWidth();

    float getHeight();

    default float getInterpolatedU(TextureAtlasSprite sprite, float u) {
        return sprite.getInterpolatedU(this.getXOffset() + u / this.getWidth());
    }

    default float getInterpolatedV(TextureAtlasSprite sprite, float v) {
        return sprite.getInterpolatedV(this.getYOffset() + v / this.getHeight());
    }

    default float[] toArray() {
        return new float[]{this.getXOffset(), this.getYOffset(), this.getXOffset() + this.getWidth(), this.getYOffset() + this.getHeight()};
    }

    default ISubmap unitScale() {
        return new SubmapRescaled(this, UNITS_PER_PIXEL, false);
    }

    default ISubmap pixelScale() {
        return this;
    }

    interface ISpriteSubmap extends ISubmap {

        TextureAtlasSprite getSprite();
    }

    float PIXELS_PER_UNIT = 16f;
    float UNITS_PER_PIXEL = 1f / PIXELS_PER_UNIT;

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString(includeFieldNames = false)
    class SubmapRescaled implements ISubmap {

        private final ISubmap parent;
        private final float ratio;
        private final boolean isPixelScale;

        @Override
        public float getXOffset() {
            return this.parent.getXOffset() * this.ratio;
        }

        @Override
        public float getYOffset() {
            return this.parent.getYOffset() * this.ratio;
        }

        @Override
        public float getWidth() {
            return this.parent.getWidth() * this.ratio;
        }

        @Override
        public float getHeight() {
            return this.parent.getHeight() * this.ratio;
        }

        @Override
        public ISubmap pixelScale() {
            return this.isPixelScale ? this : this.parent;
        }

        @Override
        public ISubmap unitScale() {
            return this.isPixelScale ? this.parent : this;
        }

        @Override
        public float getInterpolatedU(TextureAtlasSprite sprite, float u) {
            return this.parent.getInterpolatedU(sprite, u);
        }

        @Override
        public float getInterpolatedV(TextureAtlasSprite sprite, float v) {
            return this.parent.getInterpolatedV(sprite, v);
        }

        @Override
        public float[] toArray() {
            return this.parent.toArray();
        }
    }

    default ISubmap flipX() {
        return Submap.fromPixelScale(this.getWidth(), this.getHeight(), PIXELS_PER_UNIT - this.getXOffset() - this.getWidth(), this.getYOffset());
    }

    default ISubmap flipY() {
        return Submap.fromPixelScale(this.getWidth(), this.getHeight(), this.getXOffset(), PIXELS_PER_UNIT - this.getYOffset() - this.getHeight());
    }
}
