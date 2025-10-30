package michaelsebero.betterbiomeblend.client;

import net.minecraft.world.biome.BiomeColorHelper;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ColorResolverCompatibility {
    
    public static final Lock lock = new ReentrantLock();
    private static final HashMap<BiomeColorHelper.ColorResolver, Integer> knownColorResolvers = new HashMap<>();

    public static int nextColorID = BiomeColorType.LAST + 1;

    static {
        knownColorResolvers.put(BiomeColorHelper.GRASS_COLOR, BiomeColorType.GRASS);
        knownColorResolvers.put(BiomeColorHelper.WATER_COLOR, BiomeColorType.WATER);
        knownColorResolvers.put(BiomeColorHelper.FOLIAGE_COLOR, BiomeColorType.FOLIAGE);
    }

    private static int addNewColorResolver(BiomeColorHelper.ColorResolver colorResolver) {
        lock.lock();
        int id = nextColorID++;
        knownColorResolvers.put(colorResolver, id);
        lock.unlock();

        return id;
    }

    public static int getColorResolverID(BiomeColorHelper.ColorResolver colorResolver) {
        Integer id = knownColorResolvers.get(colorResolver);

        if(id == null) {
            id = addNewColorResolver(colorResolver);
        }

        return id;
    }
}