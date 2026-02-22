package team.chisel.ctm.client.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VertexData {

    private float posX, posY, posZ;
    private float normalX, normalY, normalZ;

    //Store int representations of the colors so that we don't go between ints and doubles when unpacking and repacking a vertex
    private int red, green, blue, alpha;

    // 0 to 16
    private float texU, texV;
    // 0 to 0xF0
    private int lightU, lightV;

    private Map<VertexFormatElement.EnumUsage, float[]> miscData = new EnumMap<>(VertexFormatElement.EnumUsage.class);

    public Vector3f getPos() {
        return new Vector3f(this.posX, this.posY, this.posZ);
    }

    public Vector2f getUV() {
        return new Vector2f(this.texU, this.texV);
    }

    public int getBlockLight() {
        return this.lightU >> 4;
    }

    public int getSkyLight() {
        return this.lightV >> 4;
    }

    public VertexData color(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public VertexData pos(float x, float y, float z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        return this;
    }

    public VertexData normal(float x, float y, float z) {
        this.normalX = x;
        this.normalY = y;
        this.normalZ = z;
        return this;
    }

    public VertexData texRaw(float u, float v) {
        this.texU = u;
        this.texV = v;
        return this;
    }

    public VertexData lightRaw(int u, int v) {
        this.lightU = u;
        this.lightV = v;
        return this;
    }

    public VertexData light(int u, int v) {
        return this.lightRaw(u << 4, v << 4);
    }

    public VertexData misc(VertexFormatElement element, float... data) {
        this.miscData.put(element.getUsage(), data);
        return this;
    }

    public VertexData copy(boolean deepCopy) {
        Map<VertexFormatElement.EnumUsage, float[]> miscCopy;
        if (deepCopy) {
            //Deep copy the misc data
            miscCopy = new HashMap<>();
            for (Map.Entry<VertexFormatElement.EnumUsage, float[]> entry : this.miscData.entrySet()) {
                miscCopy.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
            }
        } else {
            miscCopy = this.miscData;
        }
        return new VertexData(this.posX, this.posY, this.posZ, this.normalX, this.normalY, this.normalZ, this.red, this.green, this.blue, this.alpha, this.texU, this.texV, this.lightU, this.lightV, miscCopy);
    }

    public void pipe(IVertexConsumer consumer, VertexFormat format) {
        for (int i = 0; i < format.getElementCount(); i++) {
            VertexFormatElement ele = format.getElement(i);
            VertexFormatElement.EnumUsage usage = ele.getUsage();
            switch (usage) {
                case UV -> {
                    if (ele.getIndex() == 0) {
                        consumer.put(i, this.texU, this.texV, 0.0f, 1.0f);
                    } else if (ele.getIndex() == 1) {
                        //Stuff for fullbright
                        float bl = ((float) (this.lightU >> 4) * 32.0f) / 65535.0f;
                        float sl = ((float) (this.lightV >> 4) * 32.0f) / 65535.0f;
                        consumer.put(i, bl, sl);
                    }
                }
                case POSITION -> consumer.put(i, this.posX, this.posY, this.posZ, 1.0f);
                case COLOR -> consumer.put(i, this.red / 255.0f, this.green / 255.0f, this.blue / 255.0f, this.alpha / 255.0f);
                case NORMAL -> consumer.put(i, this.normalX, this.normalY, this.normalZ, 0.0f);
                default -> {
                    float[] data = this.miscData.get(usage);
                    if (data != null) consumer.put(i, data);
                    else consumer.put(i, new float[ele.getElementCount()]);
                }
            }
        }
    }
}
