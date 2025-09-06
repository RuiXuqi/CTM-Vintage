package team.chisel.ctm.client.newctm.json;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class Rule {
    public static final Codec<Rule> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.STRING.fieldOf("output").forGetter(Rule::output),
                    Codec.INT.optionalFieldOf("from", 0).forGetter(Rule::from),
                    Codec.STRING.optionalFieldOf("at").forGetter(Rule::at),
                    Codec.STRING.listOf().optionalFieldOf("connected", List.of()).forGetter(Rule::connected),
                    Codec.STRING.listOf().optionalFieldOf("unconnected", List.of()).forGetter(Rule::unconnected))
            .apply(i, Rule::new));
    private final String output;
    private final int from;
    private final Optional<String> at;
    private final List<String> connected;
    private final List<String> unconnected;

    public Rule(String output, int from, Optional<String> at, List<String> connected, List<String> unconnected) {
        this.output = output;
        this.from = from;
        this.at = at;
        this.connected = connected;
        this.unconnected = unconnected;
    }

    public String output() {
        return output;
    }

    public int from() {
        return from;
    }

    public Optional<String> at() {
        return at;
    }

    public List<String> connected() {
        return connected;
    }

    public List<String> unconnected() {
        return unconnected;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Rule that = (Rule) obj;
        return Objects.equals(this.output, that.output) &&
                this.from == that.from &&
                Objects.equals(this.at, that.at) &&
                Objects.equals(this.connected, that.connected) &&
                Objects.equals(this.unconnected, that.unconnected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(output, from, at, connected, unconnected);
    }

    @Override
    public String toString() {
        return "Rule[" +
                "output=" + output + ", " +
                "from=" + from + ", " +
                "at=" + at + ", " +
                "connected=" + connected + ", " +
                "unconnected=" + unconnected + ']';
    }

}
