package catboy.client;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModuleState {
    private final Map<String, Boolean> enabled = new LinkedHashMap<>();

    public ModuleState() {
        enabled.put("Brand", true);
        enabled.put("FPS", true);
        enabled.put("Coordinates", true);
        enabled.put("Keystrokes", false);
        enabled.put("Armor HUD", false);
        enabled.put("Effects HUD", false);
        enabled.put("Inventory HUD", false);
        enabled.put("Target HUD", false);
        enabled.put("Crystal Counter", false);
        enabled.put("Totem Counter", false);
        enabled.put("Durability Alerts", false);
        enabled.put("Potion Alerts", false);
        enabled.put("Zoom", false);
        enabled.put("Fullbright", false);
        enabled.put("Chunk Inspector", false);
        enabled.put("Storage Locator", false);
    }

    public Map<String, Boolean> all() { return enabled; }
    public boolean enabled(String name) { return enabled.getOrDefault(name, false); }
    public void toggle(String name) { enabled.computeIfPresent(name, (k, v) -> !v); }
}
