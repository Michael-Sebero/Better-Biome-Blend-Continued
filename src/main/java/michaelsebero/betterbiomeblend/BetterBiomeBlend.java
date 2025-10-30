package michaelsebero.betterbiomeblend;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = BetterBiomeBlend.MOD_ID,
        name = BetterBiomeBlend.MOD_NAME,
        version = BetterBiomeBlend.MOD_VERSION,
        acceptableRemoteVersions = "*"
)
public class BetterBiomeBlend {
    
    public static final String MOD_ID   = "betterbiomeblend";
    public static final String MOD_NAME = "Betterer Biomer Blender";
    public static final String MOD_VERSION = "1.2.0";
    public static final Logger LOGGER   = LogManager.getLogger(BetterBiomeBlend.MOD_ID);
    
    @Mod.Instance(BetterBiomeBlend.MOD_ID)
    public static BetterBiomeBlend INSTANCE;
}