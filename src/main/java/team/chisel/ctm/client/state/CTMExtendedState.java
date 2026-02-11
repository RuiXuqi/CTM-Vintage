package team.chisel.ctm.client.state;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.util.RenderContextList;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;
import team.chisel.ctm.client.util.ProfileUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class CTMExtendedState extends BlockStateBase implements IExtendedBlockState {

    interface Exclusions {
        <T extends Comparable<T>> T getValue(IProperty<T> property);

        <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value);

        <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property);
    }

    @Delegate(excludes = Exclusions.class)
    private final IBlockState wrapped;
    private final IBlockState clean;

    private final boolean extended;
    private final @Nullable IExtendedBlockState extState;

    @Getter
    private final IBlockAccess world;
    @Getter
    private final BlockPos pos;

    private @Nullable RenderContextList ctxCache;

    @SuppressWarnings("null")
    public CTMExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        ProfileUtil.start("ctm_extended_state");
        this.wrapped = state;
        this.world = world;
        this.pos = pos;

        this.extended = this.wrapped instanceof IExtendedBlockState;
        if (this.extended) {
            this.extState = (IExtendedBlockState) this.wrapped;
            this.clean = this.extState.getClean();
        } else {
            this.extState = null;
            this.clean = this.wrapped;
        }
        ProfileUtil.end();
    }

    public CTMExtendedState(IBlockState state, CTMExtendedState parent) {
        this(state, parent.world, parent.pos);
    }

    public RenderContextList getContextList(IBlockState state, AbstractCTMBakedModel model) {
        if (this.ctxCache == null) {
            this.ctxCache = new RenderContextList(state, model.getCTMTextures(), this.world, this.pos);
        }
        return this.ctxCache;
    }

    @Override
    public @Nullable Collection<IUnlistedProperty<?>> getUnlistedNames() {
        return this.extended ? this.extState.getUnlistedNames() : Collections.emptyList();
    }

    @Override
    public @Nullable <V> V getValue(@Nullable IUnlistedProperty<V> property) {
        return this.extended ? this.extState.getValue(property) : null;
    }

    @Override
    public <V> IExtendedBlockState withProperty(@Nullable IUnlistedProperty<V> property, @Nullable V value) {
        return this.extended ? new CTMExtendedState(this.extState.withProperty(property, value), this) : this;
    }

    @Override
    public @Nullable ImmutableMap<IUnlistedProperty<?>, Optional<?>> getUnlistedProperties() {
        return this.extended ? this.extState.getUnlistedProperties() : ImmutableMap.of();
    }

    @Override
    public IBlockState getClean() {
        return this.clean;
    }

    // Lombok chokes on these for some reason

    @Override
    public <T extends Comparable<T>> T getValue(IProperty<T> property) {
        return this.wrapped.getValue(property);
    }

    @Override
    public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
        return new CTMExtendedState(this.wrapped.withProperty(property, value), this);
    }

    @Override
    public <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property) {
        return new CTMExtendedState(this.wrapped.cycleProperty(property), this);
    }
}
