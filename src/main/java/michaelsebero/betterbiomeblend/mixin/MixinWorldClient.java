package michaelsebero.betterbiomeblend.mixin;

import michaelsebero.betterbiomeblend.client.ColorChunk;
import michaelsebero.betterbiomeblend.client.ColorChunkCache;
import michaelsebero.betterbiomeblend.client.ColorChunkCacheProvider;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient implements ColorChunkCacheProvider {
    
    @Unique
    private final ThreadLocal<ColorChunk> bbb$threadLocalGrassChunk = ThreadLocal.withInitial(() -> {
        ColorChunk chunk = new ColorChunk();
        chunk.acquire();
        return chunk;
    });

    @Unique
    private final ThreadLocal<ColorChunk> bbb$threadLocalWaterChunk = ThreadLocal.withInitial(() -> {
        ColorChunk chunk = new ColorChunk();
        chunk.acquire();
        return chunk;
    });

    @Unique
    private final ThreadLocal<ColorChunk> bbb$threadLocalFoliageChunk = ThreadLocal.withInitial(() -> {
        ColorChunk chunk = new ColorChunk();
        chunk.acquire();
        return chunk;
    });

    @Unique
    private final ThreadLocal<ColorChunk> bbb$threadLocalGenericChunk = ThreadLocal.withInitial(() -> {
        ColorChunk chunk = new ColorChunk();
        chunk.acquire();
        return chunk;
    });

    @Unique
    private final ColorChunkCache bbb$colorChunkCache = new ColorChunkCache(2048);

    @Override
    public ColorChunkCache bbb$getColorChunkCache() {
        return bbb$colorChunkCache;
    }

    @Override
    public ThreadLocal<ColorChunk> bbb$getThreadLocalGrassChunk() {
        return bbb$threadLocalGrassChunk;
    }

    @Override
    public ThreadLocal<ColorChunk> bbb$getThreadLocalWaterChunk() {
        return bbb$threadLocalWaterChunk;
    }

    @Override
    public ThreadLocal<ColorChunk> bbb$getThreadLocalFoliageChunk() {
        return bbb$threadLocalFoliageChunk;
    }

    @Override
    public ThreadLocal<ColorChunk> bbb$getThreadLocalGenericChunk() {
        return bbb$threadLocalGenericChunk;
    }
}