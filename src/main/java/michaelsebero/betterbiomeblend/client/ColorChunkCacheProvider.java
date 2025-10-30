package michaelsebero.betterbiomeblend.client;

public interface ColorChunkCacheProvider {
    
    ColorChunkCache bbb$getColorChunkCache();

    ThreadLocal<ColorChunk> bbb$getThreadLocalGrassChunk();

    ThreadLocal<ColorChunk> bbb$getThreadLocalWaterChunk();

    ThreadLocal<ColorChunk> bbb$getThreadLocalFoliageChunk();

    ThreadLocal<ColorChunk> bbb$getThreadLocalGenericChunk();
}