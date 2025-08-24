package team.chisel.ctm.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;

import javax.annotation.Nonnull;

public class ProfileUtil {
    
    /** Will never be "on" so calls to it will short-circuit */
    private static final Profiler dummyProfiler = new Profiler();
    
    private static final ThreadLocal<Profiler> profiler = ThreadLocal.withInitial(() -> {
        if (Thread.currentThread().getId() == 1) {
            return Minecraft.getMinecraft().profiler;
        } else {
            return dummyProfiler;
        }
    });
    
    public static void start(@Nonnull String section) {
        profiler.get().startSection(section);
    }
    
    public static void end() {
        profiler.get().endSection();
    }
    
    public static void endAndStart(@Nonnull String section) {
        profiler.get().endStartSection(section);
    }
}
