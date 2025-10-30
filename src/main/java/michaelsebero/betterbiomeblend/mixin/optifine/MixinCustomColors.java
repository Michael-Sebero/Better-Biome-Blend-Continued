package michaelsebero.betterbiomeblend.mixin.optifine;

import michaelsebero.betterbiomeblend.client.BiomeColor;
import michaelsebero.betterbiomeblend.client.ColorChunk;
import michaelsebero.betterbiomeblend.client.ColorChunkCache;
import michaelsebero.betterbiomeblend.client.optifine.OptifineProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.optifine.BlockPosM;
import net.optifine.CustomColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CustomColors.class)
public abstract class MixinCustomColors {
    
    /**
     * @author FionaTheMortal
     * @reason Biome color blending
     */
    @Overwrite
    private static int getSmoothColorMultiplier(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, CustomColors.IColorizer colorizer, BlockPosM blockPosM) {
        int x = blockPos.getX();
        int z = blockPos.getZ();

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        int colorizerID = OptifineProxy.getColorizerID(colorizer);
        ThreadLocal<ColorChunk> threadLocal = BiomeColor.getThreadLocalGenericChunkWrapper(blockAccess);
        ColorChunk chunk = BiomeColor.getThreadLocalChunk(threadLocal, chunkX, chunkZ, colorizerID);
        if(chunk == null) {
            ColorChunkCache cache = BiomeColor.getColorChunkCacheForIBlockAccess(blockAccess);
            chunk = OptifineProxy.getBlendedColorChunk(cache, blockAccess, colorizerID, chunkX, chunkZ, colorizer, blockState);
            BiomeColor.setThreadLocalChunk(threadLocal, chunk, cache);
        }
        
		return chunk.getColor(x, z);
    }
}