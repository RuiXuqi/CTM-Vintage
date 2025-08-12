package team.chisel.ctm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.chisel.ctm.client.model.parsing.ModelLoaderCTM;
import team.chisel.ctm.client.texture.IMetadataSectionCTM;
import team.chisel.ctm.client.texture.type.TextureTypeRegistry;
import team.chisel.ctm.client.util.CTMPackReloadListener;
import team.chisel.ctm.client.util.TextureMetadataHandler;

@Mod(name = Tags.MOD_NAME, modid = Tags.MOD_ID, version = Tags.VERSION, dependencies = "before:chisel;after:forge@[14.23.5.2807,)", clientSideOnly = true)
public class CTM {

    public static final Logger logger = LogManager.getLogger("CTM");
    
    @Mod.Instance(Tags.MOD_ID)
    public static CTM instance;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        TextureTypeRegistry.preInit(event);

        ModelLoaderRegistry.registerLoader(ModelLoaderCTM.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ModelLoaderCTM.INSTANCE);
        Minecraft.getMinecraft().metadataSerializer.registerMetadataSectionType(new IMetadataSectionCTM.Serializer(), IMetadataSectionCTM.class);
        
        MinecraftForge.EVENT_BUS.register(TextureMetadataHandler.INSTANCE);
        ((SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(CTMPackReloadListener.INSTANCE);
    }
}
