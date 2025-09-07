package team.chisel.ctm.client.texture.type;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.texture.TextureTypeList;

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
public class TextureTypeRegistry {

    private static final Map<String, ITextureType> map = Maps.newHashMap();
    public static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @SuppressWarnings("unchecked")
    public static void preInit(FMLPreInitializationEvent event) {
        try {
            lock.writeLock().lock();
            Multimap<ASMData, String> annots = HashMultimap.create();
            for (ASMData list : event.getAsmData().getAll(TextureTypeList.class.getName())) {
                for (String value : ((List<Map<String, String>>) list.getAnnotationInfo().get("value")).stream().map(m -> m.get("value")).collect(Collectors.toList())) {
                    annots.put(list, value);
                }
            }
            for (ASMData single : event.getAsmData().getAll(TextureType.class.getName())) {
                if (single.getObjectName() != null) {
                    annots.put(single, (String) single.getAnnotationInfo().get("value"));
                }
            }
            for (Entry<ASMData, Collection<String>> data : annots.asMap().entrySet()) {
                ITextureType type;
                try {
                    type = ((Class<? extends ITextureType>) Class.forName(data.getKey().getClassName())).newInstance();
                } catch (InstantiationException e) {
                    // This might be a field, let's try that
                    try {
                        Class<?> c = Class.forName(data.getKey().getClassName());
                        type = (ITextureType) c.getDeclaredField(data.getKey().getObjectName()).get(null);
                    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException |
                             SecurityException |
                             ClassNotFoundException e1) {
                        // nope
                        throw Throwables.propagate(e1);
                    }
                } catch (IllegalAccessException | ClassNotFoundException e) {
                    throw Throwables.propagate(e);
                }
                for (String name : data.getValue()) {
                    if (StringUtils.isNullOrEmpty(name)) {
                        name = data.getKey().getObjectName();
                        name = name.substring(name.lastIndexOf('.') + 1);
                    }
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
