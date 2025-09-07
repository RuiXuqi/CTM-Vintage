package team.chisel.ctm;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;

@Config(modid = Tags.MOD_ID)
@EventBusSubscriber(modid = Tags.MOD_ID)
@Config.LangKey("ctm.configuration.title")
public class Configurations {

    @Config.Comment("Disable connected textures entirely")
    @Config.LangKey("configuration.ctm.disable")
    public static boolean disableCTM = false;

    @Config.Comment("Choose whether the inside corner is disconnected on a CTM block - https://imgur.com/eUywLZ4")
    @Config.LangKey("configuration.ctm.connect_inside")
    public static boolean connectInsideCTM = false;

    @SubscribeEvent
    public static void onConfigChange(ConfigChangedEvent event) {
        if (event.getModID().equals(Tags.MOD_ID)) {
            ConfigManager.sync(Tags.MOD_ID, Type.INSTANCE);
            AbstractCTMBakedModel.invalidateCaches();
            Minecraft.getMinecraft().renderGlobal.loadRenderers();
        }
    }
}
