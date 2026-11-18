package catboy.client;

public final class Theme {
    public int accent = 0xBE00FF;
    public int secondary = 0xFF46DC;
    public int text = 0xF5E1FF;
    public int background = 0x12051C;
    public int opacity = 86;
    public int scale = 100;

    private static final int[] ACCENTS = { 0xBE00FF, 0x8A2BE2, 0xFF46DC, 0x00C8FF, 0x36D17C, 0xFF8A00, 0xFFFFFF };
    private static final int[] SECONDARIES = { 0xFF46DC, 0xBE00FF, 0x00C8FF, 0x36D17C, 0xFF8A00, 0xFFFFFF };
    private int accentIndex = 0;
    private int secondaryIndex = 0;

    public void nextAccent() {
        accentIndex = (accentIndex + 1) % ACCENTS.length;
        accent = ACCENTS[accentIndex];
    }

    public void nextSecondary() {
        secondaryIndex = (secondaryIndex + 1) % SECONDARIES.length;
        secondary = SECONDARIES[secondaryIndex];
    }
}
