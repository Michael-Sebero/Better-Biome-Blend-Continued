package michaelsebero.betterbiomeblend.client;

import java.util.concurrent.atomic.AtomicInteger;

public final class ColorChunk {
    
    public byte[] data;
    public long key;
    public int  invalidationCounter;

    public AtomicInteger refCount = new AtomicInteger();

    public ColorChunk() {
        this.data = new byte[16 * 16 * 3];
        this.markAsInvalid();
    }

    public int getReferenceCount() {
		return refCount.get();
    }

    public int release() {
		return refCount.decrementAndGet();
    }

    public void acquire() {
        refCount.incrementAndGet();
    }

    public void markAsInvalid() {
        key = (0x02000000L << 26) | (0x02000000L) | ((long)-1 << 52);
    }

    public int getColor(int x, int z) {
        int blockX = x & 15;
        int blockZ = z & 15;

        int offset = 3 * ((blockZ << 4) | blockX);

        int colorR = this.data[offset];
        int colorG = this.data[offset + 1];
        int colorB = this.data[offset + 2];
		
		return Color.makeRGBAWithFullAlpha(colorR, colorG, colorB);
    }
}