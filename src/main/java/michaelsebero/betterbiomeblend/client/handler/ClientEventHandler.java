package michaelsebero.betterbiomeblend.client.handler;

import michaelsebero.betterbiomeblend.client.BiomeColor;
import michaelsebero.betterbiomeblend.client.ColorChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler {
	
	public static final int BIOME_BLEND_RADIUS_MAX = 14;
	public static final int BIOME_BLEND_RADIUS_MIN = 0;
	
	@SubscribeEvent
	public static void onChunkLoadEvent(ChunkEvent.Load event) {
		World world = event.getWorld();
		Chunk chunk = event.getChunk();
		if(world == null || chunk == null) return;
		
		ColorChunkCache cache = BiomeColor.getColorChunkCacheForWorld(world);
		if(cache != null) cache.invalidateNeighbourhood(chunk.x, chunk.z);
	}
}