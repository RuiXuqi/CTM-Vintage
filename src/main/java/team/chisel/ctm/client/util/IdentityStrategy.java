package team.chisel.ctm.client.util;

import it.unimi.dsi.fastutil.Hash.Strategy;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class IdentityStrategy<K> implements Strategy<K> {

    @Override
    public int hashCode(@Nullable K o) {
        return Objects.hashCode(o);
    }

    @Override
    public boolean equals(@Nullable K a, @Nullable K b) {
        return Objects.equals(a, b);
    }

}
