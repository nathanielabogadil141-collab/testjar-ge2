package catboy.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class CatboyClient implements ClientModInitializer {
    public static final Theme THEME = new Theme();
    public static final ModuleState MODULES = new ModuleState();
    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("catboy-client.properties");
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        loadConfig();
        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.catboy.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyBinding.Category.MISC
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) client.setScreen(new CatboyScreen());
        });
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> renderHud(drawContext));
    }

    public static void saveConfig() {
        Properties p = new Properties();
        p.setProperty("accent", Integer.toHexString(THEME.accent));
        p.setProperty("secondary", Integer.toHexString(THEME.secondary));
        p.setProperty("text", Integer.toHexString(THEME.text));
        p.setProperty("background", Integer.toHexString(THEME.background));
        p.setProperty("opacity", Integer.toString(THEME.opacity));
        p.setProperty("scale", Integer.toString(THEME.scale));
        MODULES.all().forEach((name, enabled) -> p.setProperty("module." + name, enabled.toString()));
        try {
            Files.createDirectories(CONFIG.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG)) { p.store(out, "Catboy Client settings"); }
        } catch (IOException e) {
            System.err.println("[Catboy Client] Could not save config: " + e.getMessage());
        }
    }

    private static void loadConfig() {
        if (!Files.exists(CONFIG)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG)) {
            p.load(in);
            THEME.accent = parseColor(p, "accent", THEME.accent);
            THEME.secondary = parseColor(p, "secondary", THEME.secondary);
            THEME.text = parseColor(p, "text", THEME.text);
            THEME.background = parseColor(p, "background", THEME.background);
            THEME.opacity = clamp(parseInt(p, "opacity", THEME.opacity), 20, 100);
            THEME.scale = clamp(parseInt(p, "scale", THEME.scale), 75, 150);
            MODULES.all().replaceAll((name, oldValue) -> Boolean.parseBoolean(p.getProperty("module." + name, oldValue.toString())));
        } catch (IOException e) {
            System.err.println("[Catboy Client] Could not load config: " + e.getMessage());
        }
    }

    private static int parseColor(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key), 16) & 0xFFFFFF; }
        catch (RuntimeException e) { return fallback; }
    }
    private static int parseInt(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key)); }
        catch (RuntimeException e) { return fallback; }
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    private static void renderHud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        float scale = Math.max(0.75f, Math.min(1.5f, THEME.scale / 100.0f));
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().scale(scale, scale);
        int color = THEME.accent | 0xFF000000;
        int text = THEME.text | 0xFF000000;
        int y = 8;
        if (MODULES.enabled("Brand")) {
            panel(ctx, 8, y, 142, 22);
            ctx.drawText(mc.textRenderer, "Catboy Client 2.0", 14, y + 7, color, true);
            y += 27;
        }
        if (MODULES.enabled("FPS")) {
            panel(ctx, 8, y, 100, 20);
            ctx.drawText(mc.textRenderer, "FPS  " + mc.getCurrentFps(), 14, y + 6, text, true);
            y += 24;
        }
        if (MODULES.enabled("Coordinates")) {
            panel(ctx, 8, y, 180, 20);
            ctx.drawText(mc.textRenderer, String.format("XYZ  %.1f  %.1f  %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ()), 14, y + 6, text, true);
        }
        ctx.getMatrices().popMatrix();
    }

    private static void panel(DrawContext ctx, int x, int y, int w, int h) {
        int a = Math.max(20, Math.min(230, THEME.opacity * 255 / 100));
        int bg = (a << 24) | (THEME.background & 0x00FFFFFF);
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + 2, y + h, THEME.accent | 0xFF000000);
    }
}
