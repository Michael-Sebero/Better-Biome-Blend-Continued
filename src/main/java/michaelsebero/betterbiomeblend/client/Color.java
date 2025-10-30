package michaelsebero.betterbiomeblend.client;

public final class Color {
    
    private static final float[] sRGBLUT;

    static {
        sRGBLUT = new float[256];

        for(int i = 0; i < 256; ++i) {
            float color = byteToNormalizedFloat(i);
            sRGBLUT[i] = sRGBToLinear(color);
        }
    }
    
    public static int makeRGBAWithFullAlpha(int R, int G, int B) {
        return (0xFF & R) | ((0xFF & G) <<  8) | ((0xFF & B) << 16) | 0xFF000000;
    }
    
    public static int RGBAGetR(int color) {
		return color & 0xFF;
    }
    
    public static int RGBAGetG(int color) {
		return (color >> 8) & 0xFF;
    }
    
    public static int RGBAGetB(int color) {
		return (color >> 16) & 0xFF;
    }
    
    private static float byteToNormalizedFloat(int color) {
		return (float)color / 255.0f;
    }
    
    private static byte normalizedFloatToByte(float color) {
		return (byte)Math.round(color * 255.0f);
    }
    
    private static float sRGBToLinear(float color) {
        if(color <= 0.0404482362771082f) {
            return color / 12.92f;
        }
        else {
            return (float)Math.pow((color + 0.055f) / 1.055f, 2.4f);
        }
    }
    
    private static float linearTosRGB(float color) {
        if(color <= 0.00313066844250063f) {
            return color * 12.92f;
        }
        else {
            return 1.055f * (float)Math.pow(color, 1.0f / 2.4f) - 0.055f;
        }
    }
    
    public static float sRGBByteToLinearFloat(int color) {
		return sRGBLUT[color];
    }
    
    public static byte linearFloatTosRGBByte(float color) {
        float sRGB = linearTosRGB(color);
		return normalizedFloatToByte(sRGB);
    }
}