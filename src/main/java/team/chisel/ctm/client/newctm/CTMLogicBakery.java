package team.chisel.ctm.client.newctm;

import com.github.bsideup.jabel.Desugar;
import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.client.util.Submap;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CTMLogicBakery {

    @RequiredArgsConstructor
    private enum Trinary {
        FALSE(false, 0),
        TRUE(true, 1),
        DONT_CARE(false, 0);

        public final boolean val;
        public final int bit;
    }

    @Desugar
    private record DesiredState(Trinary[] input, int output) {
        private DesiredState(int input, int output) {
            this(new Trinary[input], output);
            Arrays.fill(this.input, Trinary.DONT_CARE);
        }

        public DesiredState with(int bit, Trinary in) {
            this.input[bit] = in;
            return this;
        }

        public boolean test(int state) {
            for (int i = 0; i < this.input.length; i++) {
                Trinary req = this.input[i];
                boolean bit = ((state >> i) & 1) == 1;
                if (req != Trinary.DONT_CARE && bit != req.val) {
                    return false;
                }
            }
            return true;
        }

        @SneakyThrows
        public String asJson(LocalDirection[] values) {
            var buf = new StringWriter();
            JsonWriter writer = new JsonWriter(buf);
            writer.beginObject();
            {
                writer.name("output").value(this.output);
                List<LocalDirection> connected = new ArrayList<>();
                List<LocalDirection> unconnected = new ArrayList<>();
                for (int i = 0; i < this.input.length; i++) {
                    if (this.input[i] == Trinary.TRUE) {
                        connected.add(values[this.input.length - 1 - i]);
                    } else if (this.input[i] == Trinary.FALSE) {
                        unconnected.add(values[this.input.length - 1 - i]);
                    }
                }
                writer.name("connected");
                writer.beginArray();
                for (var d : connected) {
                    writer.value(d.name());
                }
                writer.endArray();

                writer.name("unconnected");
                writer.beginArray();
                for (var d : unconnected) {
                    writer.value(d.name());
                }
                writer.endArray();
            }
            writer.endObject();
            writer.flush();
            writer.close();
            return buf.toString();
        }
    }

    @Desugar
    public record OutputFace(int tex, ISubmap uvs, ISubmap face) {
    }

    private int size;
    private final Int2ObjectMap<LocalDirection> bitmap = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<OutputFace> outputs = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<DesiredState> rules = new Int2ObjectOpenHashMap<>();

    public CTMLogicBakery input(int bit, LocalDirection dir) {
        this.bitmap.put(bit, dir);
        if (bit >= this.size) {
            this.size = bit + 1;
        }
        return this;
    }

    private int curRule = -1;

    public CTMLogicBakery output(int submap, ISubmap texture) {
        return this.output(submap, texture, Submap.X1);
    }

    public CTMLogicBakery output(int submap, int textureId, ISubmap texture) {
        return this.output(submap, textureId, texture, Submap.X1);
    }

    public CTMLogicBakery output(int submap, ISubmap texture, ISubmap at) {
        return this.output(submap, 0, texture, at);
    }

    public CTMLogicBakery output(int submap, int textureId, ISubmap texture, ISubmap at) {
        this.curRule = submap;
        this.outputs.put(submap, new OutputFace(textureId, texture, at));
        return this;
    }

    public CTMLogicBakery at(int submap, ISubmap at) {
        var existing = this.outputs.get(submap);
        if (existing == null) {
            throw new IllegalArgumentException("Unknown submap ID " + submap);
        }
        return this.output(submap, existing.uvs, at);
    }

    public CTMLogicBakery when(int rule, int bit, boolean is) {
        this.curRule = rule;
        return this.when(bit, is);
    }

    public CTMLogicBakery when(int bit, boolean is) {
        Preconditions.checkArgument(bit < this.size, "bit out of range");
        this.rules.putIfAbsent(this.curRule, new DesiredState(this.size, this.curRule));
        this.rules.compute(this.curRule, (i, s) -> s.with(bit, is ? Trinary.TRUE : Trinary.FALSE));
        return this;
    }

    public CTMLogicBakery when(String pattern) {
        Preconditions.checkArgument(pattern.length() == this.size, "pattern length");
        for (int i = pattern.length() - 1; i >= 0; i--) {
            char bit = pattern.charAt(i);
            if (bit == '0' || bit == '1') {
                this.when(pattern.length() - 1 - i, bit == '1');
            }
        }
        return this;
    }

    public CustomCTMLogic bake() {
        int max = 1 << this.size;
        int[][] lookups = new int[max][];
        for (int state = 0; state < max; state++) {
            for (var e : this.rules.int2ObjectEntrySet()) {
                if (e.getValue().test(state)) {
                    if (lookups[state] == null) {
                        lookups[state] = new int[]{e.getIntKey()};
                    } else {
                        lookups[state] = ArrayUtils.add(lookups[state], e.getIntKey());
                    }
                }
            }
        }
        return new CustomCTMLogic(lookups, this.asSortedArray(this.outputs, OutputFace[]::new), this.asSortedArray(this.bitmap, LocalDirection[]::new));
    }

    private <T> T[] asSortedArray(Int2ObjectMap<T> indexedMap, IntFunction<T[]> ctor) {
        return indexedMap.int2ObjectEntrySet().stream()
                .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                .map(Entry::getValue)
                .toArray(ctor);
    }

    @SneakyThrows
    public String asJsonExample() {
        var buf = new StringWriter();
        JsonWriter writer = new JsonWriter(buf);
        writer.setIndent("  ");
        writer.beginObject();
        {
            LocalDirection[] orderedPositions = this.asSortedArray(this.bitmap, LocalDirection[]::new);
            writer.name("positions");
            writer.beginArray();
            {
                for (LocalDirection dir : orderedPositions) {
                    writer.jsonValue(dir.asJson());
                }
            }
            writer.endArray();
            writer.name("submaps");
            writer.beginObject();
            {
                writer.name("type").value("grid");
                writer.name("width").value(12);
                writer.name("height").value(4);
            }
            writer.endObject();
            writer.name("rules");
            writer.beginArray();
            {
                for (var e : this.rules.int2ObjectEntrySet().stream().sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey)).collect(Collectors.toList())) {
                    writer.jsonValue(e.getValue().asJson(orderedPositions));
                }
            }
            writer.endArray();
        }
        writer.endObject();
        writer.flush();
        writer.close();
        return buf.toString();
    }
}
