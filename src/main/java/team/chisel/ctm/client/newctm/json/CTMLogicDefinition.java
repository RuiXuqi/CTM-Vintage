package team.chisel.ctm.client.newctm.json;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class CTMLogicDefinition {
    public static final Codec<CTMLogicDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Position.CODEC.listOf().fieldOf("positions").forGetter(CTMLogicDefinition::positions),
                    Codec.unboundedMap(Codec.STRING, SubmapCodecs.CODEC)
                            .fieldOf("submaps").forGetter(CTMLogicDefinition::submaps),
                    Codec.unboundedMap(Codec.STRING, SubmapCodecs.CODEC)
                            .optionalFieldOf("faces", Map.of()).forGetter(CTMLogicDefinition::faces),
                    Rule.CODEC.listOf().fieldOf("rules").forGetter(CTMLogicDefinition::rules))
            .apply(i, CTMLogicDefinition::new));
    private final List<Position> positions;
    private final Map<String, MultiSubmap> submaps;
    private final Map<String, MultiSubmap> faces;
    private final List<Rule> rules;

    public CTMLogicDefinition(List<Position> positions, Map<String, MultiSubmap> submaps, Map<String, MultiSubmap> faces, List<Rule> rules) {
        this.positions = positions;
        this.submaps = submaps;
        this.faces = faces;
        this.rules = rules;
    }

    public List<Position> positions() {
        return positions;
    }

    public Map<String, MultiSubmap> submaps() {
        return submaps;
    }

    public Map<String, MultiSubmap> faces() {
        return faces;
    }

    public List<Rule> rules() {
        return rules;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        CTMLogicDefinition that = (CTMLogicDefinition) obj;
        return Objects.equals(this.positions, that.positions) &&
                Objects.equals(this.submaps, that.submaps) &&
                Objects.equals(this.faces, that.faces) &&
                Objects.equals(this.rules, that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positions, submaps, faces, rules);
    }

    @Override
    public String toString() {
        return "CTMLogicDefinition[" +
                "positions=" + positions + ", " +
                "submaps=" + submaps + ", " +
                "faces=" + faces + ", " +
                "rules=" + rules + ']';
    }

}
