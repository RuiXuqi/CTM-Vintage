package team.chisel.ctm.client.newctm;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import java.util.Optional;

public interface ITextureConnection {

    boolean ignoreStates();

    boolean actualStates();

    boolean connectTo(ConnectionCheck ctm, IBlockState from, IBlockState to, EnumFacing dir);

    Optional<Boolean> connectInside();

    default ConnectionCheck applyTo(ConnectionCheck check) {
        check.ignoreStates(this.ignoreStates()).actualStates(this.actualStates()).stateComparator(this::connectTo);
        check.disableObscuredFaceCheck = this.connectInside();
        return check;
    }
}
