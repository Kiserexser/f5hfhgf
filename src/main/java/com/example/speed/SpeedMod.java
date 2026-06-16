package com.example.speed;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SpeedMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("speedmod");
    private static KeyBinding openMenuKey;

    @Override
    public void onInitialize() {
        LOGGER.info("Speed Mod initialized! Press Right Shift to open menu.");
        // Регистрируем клавишу
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.speedmod.openmenu",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.speedmod"
        ));
        // Обработчик нажатия
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openMenuKey.wasPressed()) {
                if (client.currentScreen instanceof ModMenuScreen) {
                    client.currentScreen.close();
                } else {
                    client.setScreen(new ModMenuScreen());
                }
            }
        });
    }

    // ==================== GUI ====================

    public static class ModMenuScreen extends Screen {
        private static final int MENU_WIDTH = 320;
        private static final int MENU_HEIGHT = 240;
        private static final int CORNER_RADIUS = 12;

        private String selectedSection = "Combat";
        private final Map<String, List<FunctionToggle>> sectionFunctions = new LinkedHashMap<>();
        private final List<SectionButton> sectionButtons = new ArrayList<>();
        private final List<ToggleButton> toggleButtons = new ArrayList<>();

        private static final int BG_COLOR = 0xCC222222;
        private static final int BORDER_COLOR = 0xFF444444;
        private static final int TEXT_WHITE = 0xFFFFFFFF;
        private static final int TEXT_HOVER = 0xFFFFB6C1;

        public ModMenuScreen() {
            super(Text.literal("Speed Mod Menu"));
            initSections();
        }

        private void initSections() {
            sectionFunctions.put("Combat", Arrays.asList(
                    new FunctionToggle("KillAura"), new FunctionToggle("Reach"),
                    new FunctionToggle("Crits"), new FunctionToggle("Velocity"),
                    new FunctionToggle("HitBox"), new FunctionToggle("AutoClicker"),
                    new FunctionToggle("WTap"), new FunctionToggle("AntiBot"),
                    new FunctionToggle("AimAssist"), new FunctionToggle("NoSlow")
            ));
            sectionFunctions.put("Movement", Arrays.asList(
                    new FunctionToggle("Sprint"), new FunctionToggle("Fly"),
                    new FunctionToggle("Speed"), new FunctionToggle("Step"),
                    new FunctionToggle("LongJump"), new FunctionToggle("BHop"),
                    new FunctionToggle("Strafe"), new FunctionToggle("Timer"),
                    new FunctionToggle("NoJumpDelay"), new FunctionToggle("WaterWalk")
            ));
            sectionFunctions.put("Player", Arrays.asList(
                    new FunctionToggle("NoFall"), new FunctionToggle("Regen"),
                    new FunctionToggle("AutoEat"), new FunctionToggle("AutoHeal"),
                    new FunctionToggle("AntiHunger"), new FunctionToggle("FastPlace"),
                    new FunctionToggle("NoPush"), new FunctionToggle("FreeCam"),
                    new FunctionToggle("InvWalk"), new FunctionToggle("Sneak")
            ));
            sectionFunctions.put("Misc", Arrays.asList(
                    new FunctionToggle("ChestSteal"), new FunctionToggle("AutoArmor"),
                    new FunctionToggle("AutoTool"), new FunctionToggle("AutoFish"),
                    new FunctionToggle("AutoSwitch"), new FunctionToggle("AntiAFK"),
                    new FunctionToggle("Schematica"), new FunctionToggle("Scaffold"),
                    new FunctionToggle("Tower"), new FunctionToggle("Parkour")
            ));
            sectionFunctions.put("Visual", Arrays.asList(
                    new FunctionToggle("ESP"), new FunctionToggle("Nametags"),
                    new FunctionToggle("Fullbright"), new FunctionToggle("Chams"),
                    new FunctionToggle("Tracers"), new FunctionToggle("Crosshair"),
                    new FunctionToggle("NoRender"), new FunctionToggle("ItemPhysics"),
                    new FunctionToggle("Zoom"), new FunctionToggle("SkyColor")
            ));
        }

        @Override
        protected void init() {
            super.init();
            int left = (this.width - MENU_WIDTH) / 2;
            int top = (this.height - MENU_HEIGHT) / 2;

            sectionButtons.clear();
            String[] sections = {"Combat", "Movement", "Player", "Misc", "Visual"};
            int y = top + 15;
            for (String sec : sections) {
                SectionButton btn = new SectionButton(left + 10, y, 80, 20, Text.literal(sec), button -> {
                    selectedSection = sec;
                    updateToggleButtons();
                });
                btn.setHoverColor(TEXT_HOVER);
                btn.setDefaultColor(TEXT_WHITE);
                sectionButtons.add(btn);
                this.addDrawableChild(btn);
                y += 25;
            }

            updateToggleButtons();
        }

        private void updateToggleButtons() {
            for (ToggleButton btn : toggleButtons) this.remove(btn);
            toggleButtons.clear();

            List<FunctionToggle> functions = sectionFunctions.get(selectedSection);
            if (functions == null) return;

            int left = (this.width - MENU_WIDTH) / 2;
            int top = (this.height - MENU_HEIGHT) / 2;
            int startX = left + 110, startY = top + 15;
            int colWidth = 90, spacing = 25;

            for (int i = 0; i < functions.size(); i++) {
                FunctionToggle func = functions.get(i);
                int col = i % 2, row = i / 2;
                int x = startX + col * colWidth;
                int y = startY + row * spacing;
                ToggleButton tb = new ToggleButton(x, y, 80, 20, Text.literal(func.name), button -> {
                    func.toggle();
                    ((ToggleButton) button).updateText(func.enabled);
                });
                tb.updateText(func.enabled);
                toggleButtons.add(tb);
                this.addDrawableChild(tb);
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, this.width, this.height, 0x88000000);
            int left = (this.width - MENU_WIDTH) / 2;
            int top = (this.height - MENU_HEIGHT) / 2;
            drawRoundRect(context, left, top, MENU_WIDTH, MENU_HEIGHT, CORNER_RADIUS, BG_COLOR, BORDER_COLOR);
            context.drawText(textRenderer, Text.literal("Speed Mod v1.0.0"), left + 10, top + 5, TEXT_WHITE, false);
            context.fill(left + 100, top + 10, left + 102, top + MENU_HEIGHT - 10, 0xFF444444);
            super.render(context, mouseX, mouseY, delta);
            for (SectionButton btn : sectionButtons) btn.setHover(btn.isHovered());
        }

        private void drawRoundRect(DrawContext context, int x, int y, int w, int h, int r, int fill, int border) {
            context.fill(x + r, y, x + w - r, y + h, fill);
            context.fill(x, y + r, x + w, y + h - r, fill);
            context.fill(x, y, x + r, y + r, fill);
            context.fill(x + w - r, y, x + w, y + r, fill);
            context.fill(x, y + h - r, x + r, y + h, fill);
            context.fill(x + w - r, y + h - r, x + w, y + h, fill);
            context.drawBorder(x, y, w, h, border);
        }

        @Override
        public boolean shouldPause() { return false; }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                this.close();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    // ---- Вспомогательные классы ----
    public static class SectionButton extends ButtonWidget {
        private int defaultColor = 0xFFFFFFFF;
        private int hoverColor = 0xFFFFB6C1;
        private boolean hovered = false;

        public SectionButton(int x, int y, int width, int height, Text message, PressAction onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        }

        public void setDefaultColor(int color) { this.defaultColor = color; }
        public void setHoverColor(int color) { this.hoverColor = color; }
        public void setHover(boolean hover) { this.hovered = hover; }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int color = hovered ? hoverColor : defaultColor;
            context.drawText(MinecraftClient.getInstance().textRenderer, this.getMessage(),
                    this.getX() + 2, this.getY() + (this.getHeight() - 8) / 2, color, false);
        }
    }

    public static class ToggleButton extends ButtonWidget {
        private boolean state = false;

        public ToggleButton(int x, int y, int width, int height, Text message, PressAction onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        }

        public void updateText(boolean enabled) {
            this.state = enabled;
            String raw = this.getMessage().getString().replaceAll("§[a-c]ON|OFF", "").trim();
            this.setMessage(Text.literal((enabled ? "§aON" : "§cOFF") + " " + raw));
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int color = this.active ? 0xFFFFFFFF : 0xFF888888;
            context.drawText(MinecraftClient.getInstance().textRenderer, this.getMessage(),
                    this.getX() + 2, this.getY() + (this.getHeight() - 8) / 2, color, false);
            context.drawBorder(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xFF666666);
            if (this.isHovered()) context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x22FFFFFF);
        }
    }

    public static class FunctionToggle {
        public final String name;
        public boolean enabled = false;
        public FunctionToggle(String name) { this.name = name; }
        public void toggle() { enabled = !enabled; }
    }
}
