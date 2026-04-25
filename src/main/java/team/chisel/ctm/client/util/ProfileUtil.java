package team.chisel.ctm.client.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;

@UtilityClass
public class ProfileUtil {

    /**
     * Will never be "on" so calls to it will short-circuit
     */
    private static final Profiler dummyProfiler = new Profiler();

    private static final ThreadLocal<Profiler> profiler = ThreadLocal.withInitial(() -> {
        if (Thread.currentThread().getId() == 1) {
            return Minecraft.getMinecraft().profiler;
        } else {
            return dummyProfiler;
        }
    });

    public static void start(String section) {
        profiler.get().startSection(section);
    }

    public static void end() {
        profiler.get().endSection();
    }

    public static void endAndStart(String section) {
        profiler.get().endStartSection(section);
    }
}
