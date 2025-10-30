package michaelsebero.betterbiomeblend.config;

import michaelsebero.betterbiomeblend.BetterBiomeBlend;
import michaelsebero.betterbiomeblend.client.BiomeColor;
import michaelsebero.betterbiomeblend.client.handler.ClientEventHandler;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Config(modid = BetterBiomeBlend.MOD_ID)
public class BetterBiomeBlendConfig {
    
    @Config.Name("Blend Radius")
    @Config.Comment("Higher values = smoother blending but more performance impact")
    @Config.RangeInt(
        min = ClientEventHandler.BIOME_BLEND_RADIUS_MIN,
        max = ClientEventHandler.BIOME_BLEND_RADIUS_MAX)
    @Config.RequiresWorldRestart
    public static int blendRadius = 14;
    
    @Mod.EventBusSubscriber(modid = BetterBiomeBlend.MOD_ID)
    private static class EventHandler {
        
        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(BetterBiomeBlend.MOD_ID)) {
                ConfigManager.sync(BetterBiomeBlend.MOD_ID, Config.Type.INSTANCE);
                
                // Clear blend caches when config changes
                BiomeColor.clearBlendCaches();
            }
        }
    }
}
