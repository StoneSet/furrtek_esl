package net.sourceforge.zbar;

public class ImageScanner {
    private long peer = create();

    static {
        System.loadLibrary("zbarjni");
        init();
    }

    private native long create();

    private native void destroy(long j);

    private native long getResults(long j);

    private static native void init();

    public void destroy() {
        synchronized (this) {
            if (this.peer != 0) {
                destroy(this.peer);
                this.peer = 0;
            }
        }
    }

    public native void enableCache(boolean z);

    protected void finalize() {
        destroy();
    }

    public SymbolSet getResults() {
        return new SymbolSet(getResults(this.peer));
    }

    public native void parseConfig(String str);

    public native int scanImage(Image image);

    public native void setConfig(int i, int i2, int i3) throws IllegalArgumentException;
}
