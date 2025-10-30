package michaelsebero.betterbiomeblend.client;

import michaelsebero.betterbiomeblend.client.handler.ClientEventHandler;
import michaelsebero.betterbiomeblend.client.optifine.OptifineCompatibility;
import michaelsebero.betterbiomeblend.config.BetterBiomeBlendConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public final class BiomeColor {
    
    // Use ArrayDeque instead of Stack for better performance
    private static final ReentrantLock freeBlendCachesLock = new ReentrantLock();
    private static final Queue<ColorBlendCache> freeBlendCaches = new ArrayDeque<>();
    
    // Pre-computed lookup tables for better cache locality
    private static final byte[] NEIGHBOR_OFFSETS = {
            -1, -1, 0, -1, 1, -1,
            -1,  0, 0,  0, 1,  0,
            -1,  1, 0,  1, 1,  1
    };
    
    private static final byte[] NEIGHBOR_RECT_PARAMS = {
            -1, -1,  0,  0, -16, -16,  0,  0,
             0, -1,  0,  0,   0, -16,  0,  0,
             0, -1, -1,  0,  16, -16,  0,  0,
            -1,  0,  0,  0, -16,   0,  0,  0,
             0,  0,  0,  0,   0,   0,  0,  0,
             0,  0, -1,  0,  16,   0,  0,  0,
            -1,  0,  0, -1, -16,  16,  0,  0,
             0,  0,  0, -1,   0,  16,  0,  0,
             0,  0, -1, -1,  16,  16,  0,  0
    };
    
    // Cache frequently accessed values
    private static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_POS = 
        ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    
    private static int getNeighborOffsetX(int chunkIndex) {
        return NEIGHBOR_OFFSETS[2 * chunkIndex];
    }
    
    private static int getNeighborOffsetZ(int chunkIndex) {
        return NEIGHBOR_OFFSETS[2 * chunkIndex + 1];
    }
    
    private static int getNeighborRectMinX(int chunkIndex, int radius) {
        return NEIGHBOR_RECT_PARAMS[8 * chunkIndex] & (16 - radius);
    }
    
    private static int getNeighborRectMinZ(int chunkIndex, int radius) {
        return NEIGHBOR_RECT_PARAMS[8 * chunkIndex + 1] & (16 - radius);
    }

    private static int getNeighborRectMaxX(int chunkIndex, int radius) {
        return (NEIGHBOR_RECT_PARAMS[8 * chunkIndex + 2] & (radius - 16)) + 16;
    }
    
    private static int getNeighborRectMaxZ(int chunkIndex, int radius) {
        return (NEIGHBOR_RECT_PARAMS[8 * chunkIndex + 3] & (radius - 16)) + 16;
    }
    
    private static int getNeighborRectBlendCacheMinX(int chunkIndex, int radius) {
        return Math.max(NEIGHBOR_RECT_PARAMS[8 * chunkIndex + 4] + radius, 0);
    }
    
    private static int getNeighborRectBlendCacheMinZ(int chunkIndex, int radius) {
        return Math.max(NEIGHBOR_RECT_PARAMS[8 * chunkIndex + 5] + radius, 0);
    }

    public static void clearBlendCaches() {
        freeBlendCachesLock.lock();
        try {
            freeBlendCaches.clear();
        } finally {
            freeBlendCachesLock.unlock();
        }
    }

    private static ColorBlendCache acquireBlendCache(int blendRadius) {
        ColorBlendCache result = null;

        freeBlendCachesLock.lock();
        try {
            // Try to find a cache with matching radius
            for (ColorBlendCache cache : freeBlendCaches) {
                if (cache.blendRadius == blendRadius) {
                    freeBlendCaches.remove(cache);
                    result = cache;
                    break;
                }
            }
        } finally {
            freeBlendCachesLock.unlock();
        }

        if (result == null) {
            result = new ColorBlendCache(blendRadius);
        }

        return result;
    }
    
    private static void releaseBlendCache(ColorBlendCache cache) {
        int blendRadius = BetterBiomeBlendConfig.blendRadius;
        
        freeBlendCachesLock.lock();
        try {
            if (cache.blendRadius == blendRadius && freeBlendCaches.size() < 64) {
                freeBlendCaches.offer(cache);
            }
        } finally {
            freeBlendCachesLock.unlock();
        }
    }
    
    public static ThreadLocal<ColorChunk> getThreadLocalGrassChunkWrapper(IBlockAccess blockAccess) {
        World world = getWorldFromBlockAccess(blockAccess);
        
        if (world instanceof ColorChunkCacheProvider) {
            return ((ColorChunkCacheProvider)world).bbb$getThreadLocalGrassChunk();
        }
        return StaticCompatibilityCache.getThreadLocalGrassChunkWrapper();
    }
    
    public static ThreadLocal<ColorChunk> getThreadLocalWaterChunkWrapper(IBlockAccess blockAccess) {
        World world = getWorldFromBlockAccess(blockAccess);

        if (world instanceof ColorChunkCacheProvider) {
            return ((ColorChunkCacheProvider)world).bbb$getThreadLocalWaterChunk();
        }
        return StaticCompatibilityCache.getThreadLocalWaterChunkWrapper();
    }
    
    public static ThreadLocal<ColorChunk> getThreadLocalFoliageChunkWrapper(IBlockAccess blockAccess) {
        World world = getWorldFromBlockAccess(blockAccess);

        if (world instanceof ColorChunkCacheProvider) {
            return ((ColorChunkCacheProvider)world).bbb$getThreadLocalFoliageChunk();
        }
        return StaticCompatibilityCache.getThreadLocalFoliageChunkWrapper();
    }
    
    public static ThreadLocal<ColorChunk> getThreadLocalGenericChunkWrapper(IBlockAccess blockAccess) {
        World world = getWorldFromBlockAccess(blockAccess);

        if (world instanceof ColorChunkCacheProvider) {
            return ((ColorChunkCacheProvider)world).bbb$getThreadLocalGenericChunk();
        }
        return StaticCompatibilityCache.getThreadLocalGenericChunkWrapper();
    }
    
    public static ColorChunk getThreadLocalChunk(ThreadLocal<ColorChunk> threadLocal, int chunkX, int chunkZ, int colorType) {
        ColorChunk local = threadLocal.get();
        if (local.key == ColorChunkCache.getChunkKey(chunkX, chunkZ, colorType)) {
            return local;
        }
        return null;
    }
    
    public static void setThreadLocalChunk(ThreadLocal<ColorChunk> threadLocal, ColorChunk chunk, ColorChunkCache cache) {
        ColorChunk local = threadLocal.get();
        cache.releaseChunk(local);
        threadLocal.set(chunk);
    }
    
    private static void gatherRawColorsForChunk(IBlockAccess blockAccess, byte[] result, int chunkX, int chunkZ, BiomeColorHelper.ColorResolver colorResolver) {
        BlockPos.MutableBlockPos blockPos = MUTABLE_POS.get();

        int blockX = chunkX << 4;
        int blockZ = chunkZ << 4;

        int dstIndex = 0;

        for (int z = 0; z < 16; ++z) {
            for (int x = 0; x < 16; ++x) {
                blockPos.setPos(blockX + x, 0, blockZ + z);

                int color = colorResolver.getColorAtPos(blockAccess.getBiome(blockPos), blockPos);
                
                result[dstIndex++] = (byte)(color & 0xFF);
                result[dstIndex++] = (byte)((color >> 8) & 0xFF);
                result[dstIndex++] = (byte)((color >> 16) & 0xFF);
            }
        }
    }

    private static void gatherRawColorsToBlendCache(IBlockAccess blockAccess, int chunkX, int chunkZ, int blendRadius, byte[] result, int chunkIndex, BiomeColorHelper.ColorResolver colorResolver) {
        BlockPos.MutableBlockPos blockPos = MUTABLE_POS.get();

        int blockX = chunkX << 4;
        int blockZ = chunkZ << 4;

        int srcMinX = getNeighborRectMinX(chunkIndex, blendRadius);
        int srcMinZ = getNeighborRectMinZ(chunkIndex, blendRadius);
        int srcMaxX = getNeighborRectMaxX(chunkIndex, blendRadius);
        int srcMaxZ = getNeighborRectMaxZ(chunkIndex, blendRadius);
        int dstMinX = getNeighborRectBlendCacheMinX(chunkIndex, blendRadius);
        int dstMinZ = getNeighborRectBlendCacheMinZ(chunkIndex, blendRadius);

        int dstDim = 16 + 2 * blendRadius;
        int dstLine = 3 * (dstMinX + dstMinZ * dstDim);

        for (int z = srcMinZ; z < srcMaxZ; ++z) {
            int dstIndex = dstLine;
            for (int x = srcMinX; x < srcMaxX; ++x) {
                blockPos.setPos(blockX + x, 0, blockZ + z);

                int color = colorResolver.getColorAtPos(blockAccess.getBiome(blockPos), blockPos);

                result[dstIndex++] = (byte)(color & 0xFF);
                result[dstIndex++] = (byte)((color >> 8) & 0xFF);
                result[dstIndex++] = (byte)((color >> 16) & 0xFF);
            }
            dstLine += 3 * dstDim;
        }
    }

    private static void gatherRawColorsToBlendCache(IBlockAccess blockAccess, int chunkX, int chunkZ, int blendRadius, byte[] result, BiomeColorHelper.ColorResolver colorResolver) {
        for (int chunkIndex = 0; chunkIndex < 9; ++chunkIndex) {
            int offsetX = getNeighborOffsetX(chunkIndex);
            int offsetZ = getNeighborOffsetZ(chunkIndex);

            int rawChunkX = chunkX + offsetX;
            int rawChunkZ = chunkZ + offsetZ;

            gatherRawColorsToBlendCache(blockAccess, rawChunkX, rawChunkZ, blendRadius, result, chunkIndex, colorResolver);
        }
    }

    // Optimized blending using separable box filter
    private static void blendCachedColorsForChunk(byte[] result, ColorBlendCache blendCache) {
        float[] R = blendCache.R;
        float[] G = blendCache.G;
        float[] B = blendCache.B;

        int blendRadius = blendCache.blendRadius;
        int blendDim = 2 * blendRadius + 1;
        int blendCacheDim = 16 + 2 * blendRadius;
        float invBlendCount = 1.0f / (blendDim * blendDim);

        // Horizontal pass - convert to linear and accumulate
        for (int x = 0; x < blendCacheDim; ++x) {
            int idx = 3 * x;
            R[x] = Color.sRGBByteToLinearFloat(0xFF & blendCache.color[idx]);
            G[x] = Color.sRGBByteToLinearFloat(0xFF & blendCache.color[idx + 1]);
            B[x] = Color.sRGBByteToLinearFloat(0xFF & blendCache.color[idx + 2]);
        }

        for (int z = 1; z < blendDim; ++z) {
            int rowOffset = blendCacheDim * z;
            for (int x = 0; x < blendCacheDim; ++x) {
                int idx = 3 * (rowOffset + x);
                R[x] += Color.sRGBByteToLinearFloat(0xFF & blendCache.color[idx]);
                G[x] += Color.sRGBByteToLinearFloat(0xFF & blendCache.color[idx + 1]);
                B[x] += Color.sRGBByteToLinearFloat(0xFF & blendCache.color[idx + 2]);
            }
        }

        // Vertical pass and output
        for (int z = 0; z < 16; ++z) {
            float accR = 0, accG = 0, accB = 0;

            for (int x = 0; x < blendDim; ++x) {
                accR += R[x];
                accG += G[x];
                accB += B[x];
            }

            int rowStart = 48 * z; // 3 * 16 * z

            for (int x = 0; x < 16; ++x) {
                int dstIdx = rowStart + 3 * x;
                
                result[dstIdx] = Color.linearFloatTosRGBByte(accR * invBlendCount);
                result[dstIdx + 1] = Color.linearFloatTosRGBByte(accG * invBlendCount);
                result[dstIdx + 2] = Color.linearFloatTosRGBByte(accB * invBlendCount);

                if (x < 15) {
                    accR += R[x + blendDim] - R[x];
                    accG += G[x + blendDim] - G[x];
                    accB += B[x + blendDim] - B[x];
                }
            }

            if (z < 15) {
                int oldRow = blendCacheDim * z;
                int newRow = blendCacheDim * (z + blendDim);
                
                for (int x = 0; x < blendCacheDim; ++x) {
                    int oldIdx = 3 * (oldRow + x);
                    int newIdx = 3 * (newRow + x);

                    R[x] += Color.sRGBByteToLinearFloat(0xFF & blendCache.color[newIdx]) 
                          - Color.sRGBByteToLinearFloat(0xFF & blendCache.color[oldIdx]);
                    G[x] += Color.sRGBByteToLinearFloat(0xFF & blendCache.color[newIdx + 1]) 
                          - Color.sRGBByteToLinearFloat(0xFF & blendCache.color[oldIdx + 1]);
                    B[x] += Color.sRGBByteToLinearFloat(0xFF & blendCache.color[newIdx + 2]) 
                          - Color.sRGBByteToLinearFloat(0xFF & blendCache.color[oldIdx + 2]);
                }
            }
        }
    }

    public static void generateBlendedColorChunk(IBlockAccess blockAccess, int chunkX, int chunkZ, byte[] result, int colorType, BiomeColorHelper.ColorResolver colorResolver) {
        int blendRadius = BetterBiomeBlendConfig.blendRadius;

        if (blendRadius > ClientEventHandler.BIOME_BLEND_RADIUS_MIN && blendRadius <= ClientEventHandler.BIOME_BLEND_RADIUS_MAX) {
            ColorBlendCache blendCache = acquireBlendCache(blendRadius);
            gatherRawColorsToBlendCache(blockAccess, chunkX, chunkZ, blendCache.blendRadius, blendCache.color, colorResolver);
            blendCachedColorsForChunk(result, blendCache);
            releaseBlendCache(blendCache);
        } else {
            gatherRawColorsForChunk(blockAccess, result, chunkX, chunkZ, colorResolver);
        }
    }
    
    private static World getWorldFromBlockAccess(IBlockAccess blockAccess) {
        if (blockAccess instanceof World) {
            return (World)blockAccess;
        }
        
        if (blockAccess instanceof ChunkCache) {
            return ((ChunkCache)blockAccess).world;
        }
        
        if (OptifineCompatibility.isChunkCacheOF(blockAccess)) {
            ChunkCache chunkCache = OptifineCompatibility.getChunkCacheFromChunkCacheOF(blockAccess);
            if (chunkCache != null) {
                return chunkCache.world;
            }
        }

        return null;
    }

    public static ColorChunkCache getColorChunkCacheForWorld(World world) {
        if (world instanceof ColorChunkCacheProvider) {
            return ((ColorChunkCacheProvider)world).bbb$getColorChunkCache();
        }
        return StaticCompatibilityCache.getColorChunkCache();
    }
    
    public static ColorChunkCache getColorChunkCacheForIBlockAccess(IBlockAccess blockAccess) {
        World world = getWorldFromBlockAccess(blockAccess);
        return getColorChunkCacheForWorld(world);
    }
    
    public static ColorChunk getBlendedColorChunk(ColorChunkCache cache, IBlockAccess blockAccess, int colorID, int chunkX, int chunkZ, BiomeColorHelper.ColorResolver colorResolver) {
        ColorChunk chunk = cache.getChunk(chunkX, chunkZ, colorID);
        if (chunk == null) {
            chunk = cache.newChunk(chunkX, chunkZ, colorID);
            generateBlendedColorChunk(blockAccess, chunkX, chunkZ, chunk.data, colorID, colorResolver);
            cache.putChunk(chunk);
        }

        return chunk;
    }
}
