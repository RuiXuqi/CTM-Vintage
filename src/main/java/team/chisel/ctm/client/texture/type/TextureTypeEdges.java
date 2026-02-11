package team.chisel.ctm.client.texture.type;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.newctm.ConnectionCheck;
import team.chisel.ctm.client.texture.ctx.TextureContextCTM;
import team.chisel.ctm.client.texture.render.TextureEdges;
import team.chisel.ctm.client.util.CTMLogic;
import team.chisel.ctm.client.util.Dir;

import javax.annotation.ParametersAreNonnullByDefault;

@TextureType("edges")
public class TextureTypeEdges extends TextureTypeCTM {

    @Override
    public ICTMTexture<? extends TextureTypeCTM> makeTexture(@NotNull TextureInfo info) {
        return new TextureEdges(this, info);
    }

    @ParametersAreNonnullByDefault
    public static class CTMLogicEdges extends CTMLogic {

        public CTMLogicEdges() {
            this.connectionCheck = new ConnectionCheckEdges();
        }

        @Override
        protected void fillSubmaps(int idx) {
            Dir[] dirs = submapMap[idx];
            if (!this.connectedOr(dirs[0], dirs[1]) && this.connected(dirs[2])) {
                this.submapCache[idx] = submapOffsets[idx];
            } else {
                super.fillSubmaps(idx);
            }
        }

        @Override
        public long serialized() {
            return this.isObscured() ? (super.serialized() | (1 << 8)) : super.serialized();
        }

        public boolean isObscured() {
            return ((ConnectionCheckEdges) this.connectionCheck).isObscured();
        }
    }

    public static class ConnectionCheckEdges extends ConnectionCheck {

        @Setter
        @Getter
        private boolean obscured;

        @Override
        public boolean isConnected(IBlockAccess world, BlockPos current, IBlockState currentState, BlockPos connection, EnumFacing dir, IBlockState state) {
            if (this.isObscured()) {
                return false;
            }
            IBlockState obscuring = this.getConnectionState(world, current.offset(dir), dir, current, currentState);
            if (this.stateComparator(state, obscuring, dir)) {
                this.setObscured(true);
                return false;
            }

            IBlockState con = this.getConnectionState(world, connection, dir, current, currentState);
            IBlockState obscuringcon = this.getConnectionState(world, connection.offset(dir), dir, current, currentState);

            if (this.stateComparator(state, con, dir) || this.stateComparator(state, obscuringcon, dir)) {
                Vec3d difference = new Vec3d(connection.subtract(current));
                if (difference.lengthSquared() > 1) {
                    difference = difference.normalize();
                    if (dir.getAxis() == EnumFacing.Axis.Z) {
                        difference = difference.rotateYaw((float) (-Math.PI / 2));
                    }
                    float ang = (float) Math.PI / 4;
                    Vec3d vA, vB;
                    if (dir.getAxis().isVertical()) {
                        vA = difference.rotateYaw(ang);
                        vB = difference.rotateYaw(-ang);
                    } else {
                        vA = difference.rotatePitch(ang);
                        vB = difference.rotatePitch(-ang);
                    }
                    BlockPos posA = new BlockPos(vA).add(current);
                    BlockPos posB = new BlockPos(vB).add(current);
                    return (this.getConnectionState(world, posA, dir, current, currentState) == state && !this.stateComparator(state, this.getConnectionState(world, posA.offset(dir), dir, current, currentState), dir))
                            || (this.getConnectionState(world, posB, dir, current, currentState) == state && !this.stateComparator(state, this.getConnectionState(world, posB.offset(dir), dir, current, currentState), dir));
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public TextureContextCTM getBlockRenderContext(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull ICTMTexture<?> tex) {
        return new TextureContextCTM(state, world, pos, (TextureEdges) tex) {

            @Override
            protected @NotNull CTMLogic createCTM(@NotNull IBlockState state) {
                CTMLogic parent = super.createCTM(state);
                // FIXME
                CTMLogic ret = new CTMLogicEdges();
                ret.connectionCheck.ignoreStates(parent.connectionCheck.ignoreStates()).stateComparator(parent.connectionCheck.stateComparator());
                ret.connectionCheck.disableObscuredFaceCheck = parent.connectionCheck.disableObscuredFaceCheck;
                return ret;
            }
        };
    }

    @Override
    public int requiredTextures() {
        return 3;
    }
}
