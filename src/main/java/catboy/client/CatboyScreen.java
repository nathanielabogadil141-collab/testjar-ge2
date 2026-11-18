package catboy.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CatboyScreen extends Screen {
    private TextFieldWidget searchField;
    private int scroll = 0;
    private final List<Row> rows = new ArrayList<>();
    private record Row(String name, int y) { }

    public CatboyScreen() { super(Text.literal("Catboy Client")); }

    @Override
    protected void init() {
        int cx = width / 2;
        searchField = new TextFieldWidget(textRenderer, cx - 190, 54, 380, 20, Text.literal("Search modules"));
        searchField.setMaxLength(48);
        searchField.setPlaceholder(Text.literal("Search modules..."));
        addDrawableChild(searchField);

        addDrawableChild(ButtonWidget.builder(accentLabel(), b -> {
            CatboyClient.THEME.nextAccent();
            CatboyClient.saveConfig();
            b.setMessage(accentLabel());
        }).dimensions(cx - 190, height - 48, 120, 20).build());
        addDrawableChild(ButtonWidget.builder(secondaryLabel(), b -> {
            CatboyClient.THEME.nextSecondary();
            CatboyClient.saveConfig();
            b.setMessage(secondaryLabel());
        }).dimensions(cx - 65, height - 48, 120, 20).build());
        addDrawableChild(ButtonWidget.builder(opacityLabel(), b -> {
            CatboyClient.THEME.opacity = CatboyClient.THEME.opacity >= 100 ? 30 : CatboyClient.THEME.opacity + 10;
            CatboyClient.saveConfig();
            b.setMessage(opacityLabel());
        }).dimensions(cx + 60, height - 48, 85, 20).build());
        addDrawableChild(ButtonWidget.builder(scaleLabel(), b -> {
            CatboyClient.THEME.scale = CatboyClient.THEME.scale >= 150 ? 75 : CatboyClient.THEME.scale + 25;
            CatboyClient.saveConfig();
            b.setMessage(scaleLabel());
        }).dimensions(cx - 45, height - 24, 90, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(cx + 52, height - 24, 80, 20).build());
    }

    private static Text accentLabel() { return Text.literal("Accent #" + hex(CatboyClient.THEME.accent)); }
    private static Text secondaryLabel() { return Text.literal("Glow #" + hex(CatboyClient.THEME.secondary)); }
    private static Text opacityLabel() { return Text.literal("Opacity " + CatboyClient.THEME.opacity + "%"); }
    private static Text scaleLabel() { return Text.literal("HUD " + CatboyClient.THEME.scale + "%"); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int accent = CatboyClient.THEME.accent | 0xFF000000;
        int left = width / 2 - 205;
        int top = 24;
        ctx.fill(left, top, left + 410, height - 58, 0xE612051C);
        ctx.fill(left, top, left + 410, top + 3, accent);
        ctx.drawCenteredTextWithShadow(textRenderer, "CATBOY CLIENT  •  FABRIC 1.21.11", width / 2, 34, accent);
        ctx.drawText(textRenderer, "MODULE SEARCH", left + 15, 44, 0xFFD8B9E8, false);

        rows.clear();
        String filter = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        int y = 82;
        int shown = 0;
        for (Map.Entry<String, Boolean> entry : CatboyClient.MODULES.all().entrySet()) {
            if (!entry.getKey().toLowerCase().contains(filter)) continue;
            if (shown++ < scroll) continue;
            if (y > height - 100) break;
            rows.add(new Row(entry.getKey(), y));
            ctx.fill(left + 12, y - 2, left + 398, y + 17, 0x9B260D35);
            ctx.drawText(textRenderer, entry.getKey(), left + 22, y + 2, 0xFFF5E1FF, false);
            boolean enabled = entry.getValue();
            ctx.drawText(textRenderer, enabled ? "ON" : "OFF", left + 365, y + 2,
                enabled ? accent : 0xFF888888, false);
            y += 22;
        }
        ctx.drawCenteredTextWithShadow(textRenderer, "Click a module to toggle  •  Mouse wheel to scroll",
            width / 2, height - 72, 0xFFD8B9E8);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int left = width / 2 - 205;
        for (Row row : rows) {
            if (mouseX >= left + 12 && mouseX <= left + 398 && mouseY >= row.y - 2 && mouseY <= row.y + 17) {
                CatboyClient.MODULES.toggle(row.name);
                CatboyClient.saveConfig();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, Math.min(Math.max(0, CatboyClient.MODULES.all().size() - 1),
            scroll + (verticalAmount < 0 ? 1 : -1)));
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == 256) { close(); return true; }
        return super.keyPressed(input);
    }

    private static String hex(int color) { return String.format("%06X", color & 0xFFFFFF); }
}
