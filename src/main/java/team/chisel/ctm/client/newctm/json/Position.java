package team.chisel.ctm.client.newctm.json;

import java.util.List;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.EnumFacing;

public final class Position {
    public static final Codec<Position> CODEC = RecordCodecBuilder.create(i ->
            i.group(Codec.STRING.fieldOf("id").forGetter(Position::id),
                            EnumFacing.CODEC.listOf().fieldOf("directions").forGetter(Position::directions))
                    .apply(i, Position::new));
    private final String id;
    private final List<EnumFacing> directions;

    public Position(String id, List<EnumFacing> directions) {
        this.id = id;
        this.directions = directions;
    }

    public String id() {
        return id;
    }

    public List<EnumFacing> directions() {
        return directions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Position that = (Position) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.directions, that.directions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, directions);
    }

    @Override
    public String toString() {
        return "Position[" +
                "id=" + id + ", " +
                "directions=" + directions + ']';
    }

}
