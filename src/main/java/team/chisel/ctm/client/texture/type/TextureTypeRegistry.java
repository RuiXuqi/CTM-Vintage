package team.chisel.ctm.client.texture.type;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.texture.TextureTypeList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Registry for all the different texture types
 */
@Log4j2
@UtilityClass
public class TextureTypeRegistry {

    private static final Map<String, ITextureType> map = Maps.newHashMap();
    public static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static void preInit(FMLPreInitializationEvent event) {
        try {
            lock.writeLock().lock();

            // Collect needed ASM data
            final ASMDataTable table = event.getAsmData();
            Multimap<ASMDataTable.ASMData, String> annots = HashMultimap.create();
            for (ASMDataTable.ASMData list : table.getAll(TextureTypeList.class.getName())) {
                //noinspection unchecked
                for (String value : ((List<Map<String, String>>) list.getAnnotationInfo().get("value")).stream().map(m -> m.get("value")).collect(Collectors.toList())) {
                    annots.put(list, value);
                }
            }
            for (ASMDataTable.ASMData single : table.getAll(TextureType.class.getName())) {
                if (single.getObjectName() != null) {
                    annots.put(single, (String) single.getAnnotationInfo().get("value"));
                }
            }
            log.debug("Found {} unique texture types", annots.size());

            for (Entry<ASMDataTable.ASMData, Collection<String>> data : annots.asMap().entrySet()) {
                ITextureType type;
                final ASMDataTable.ASMData asmData = data.getKey();

                String className = asmData.getClassName();
                String objectName = asmData.getObjectName(); // Class or field name

                // Construct
                if (objectName.equals(className)) {
                    // Class
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (ITextureType.class.isAssignableFrom(clazz)) {
                            type = (ITextureType) clazz.getDeclaredConstructor().newInstance();
                        } else {
                            throw new RuntimeException(className + " must implement ITextureType.");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Exception loading texture type for class: " + className, e);
                    }
                } else {
                    // Field
                    try {
                        Class<?> clazz = Class.forName(className);
                        Field field = clazz.getDeclaredField(objectName);
                        field.setAccessible(true);
                        Object obj = field.get(null);
                        if (obj instanceof ITextureType) {
                            type = (ITextureType) obj;
                        } else {
                            throw new RuntimeException(objectName + " is not an instance of ITextureType.");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Exception loading texture type for class: " + className + " (on member " + objectName + ")", e);
                    }
                }

                // Register
                for (String name : data.getValue()) {
                    if (StringUtils.isNullOrEmpty(name)) {
                        name = data.getKey().getObjectName();
                        name = name.substring(name.lastIndexOf('.') + 1);
                    }
                    log.debug("Registering scanned texture type: {}", name);
                    register(name, type);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void register(String name, ITextureType type) {
        try {
            lock.writeLock().lock();
            String key = name.toLowerCase(Locale.ROOT);
            if (map.containsKey(key) && map.get(key) != type) {
                throw new IllegalArgumentException("Render Type with name " + key + " has already been registered!");
            } else if (map.get(key) != type) {
                map.put(key, type);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static ITextureType remove(String name) {
        try {
            lock.writeLock().lock();
            String key = name.toLowerCase(Locale.ROOT);
            return map.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static ITextureType getType(String name) {
        try {
            lock.readLock().lock();
            String key = name.toLowerCase(Locale.ROOT);
            return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static boolean isValid(String name) {
        return getType(name) != null;
    }
}
