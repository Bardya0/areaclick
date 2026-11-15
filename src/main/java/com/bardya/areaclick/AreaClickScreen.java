package com.bardya.areaclick;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class AreaClickScreen extends Screen {
    private Button toggleBtn, centerBtn, keyBtn;
    private EditBox widthBox, depthBox, heightBox;
    private boolean waitingForKey = false;

    public AreaClickScreen() {
        super(Component.literal("Area Click"));
    }

    @Override
    protected void init() {
        int w = 200, h = 20, x = this.width / 2 - w / 2;
        int y = this.height / 2 - 80;

        toggleBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            AreaClick.enabled = !AreaClick.enabled;
            AreaClick.resetState();
            refresh();
        }).bounds(x, y, w, h).build());

        // اندازه محدوده: عرض × طول × ارتفاع
        int bw = 62;
        widthBox = new EditBox(this.font, x, y + 40, bw, h, Component.literal("Width"));
        widthBox.setFilter(s -> s.matches("\\d{0,3}"));
        widthBox.setValue(String.valueOf(AreaClick.width));
        widthBox.setResponder(s -> {
            if (!s.isEmpty()) AreaClick.width = Math.max(1, Integer.parseInt(s));
            AreaClick.resetState();
        });
        addRenderableWidget(widthBox);

        depthBox = new EditBox(this.font, x + bw + 7, y + 40, bw, h, Component.literal("Depth"));
        depthBox.setFilter(s -> s.matches("\\d{0,3}"));
        depthBox.setValue(String.valueOf(AreaClick.depth));
        depthBox.setResponder(s -> {
            if (!s.isEmpty()) AreaClick.depth = Math.max(1, Integer.parseInt(s));
            AreaClick.resetState();
        });
        addRenderableWidget(depthBox);

        heightBox = new EditBox(this.font, x + 2 * (bw + 7), y + 40, bw, h, Component.literal("Height"));
        heightBox.setFilter(s -> s.matches("\\d{0,3}"));
        heightBox.setValue(String.valueOf(AreaClick.height));
        heightBox.setResponder(s -> {
            if (!s.isEmpty()) AreaClick.height = Integer.parseInt(s); // 0 = ارتفاع چک نشه
            AreaClick.resetState();
        });
        addRenderableWidget(heightBox);

        centerBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                AreaClick.centerX = mc.player.getX();
                AreaClick.centerY = mc.player.getY();
                AreaClick.centerZ = mc.player.getZ();
                AreaClick.centerSet = true;
                AreaClick.resetState();
                refresh();
            }
        }).bounds(x, y + 66, w, h).build());

        keyBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            waitingForKey = true;
            refresh();
        }).bounds(x, y + 92, w, h).build());

        addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
                .bounds(x, y + 126, w, h).build());

        refresh();
    }

    private void refresh() {
        toggleBtn.setMessage(Component.literal("Area Click: " + (AreaClick.enabled ? "ON" : "OFF")));
        centerBtn.setMessage(Component.literal(AreaClick.centerSet
                ? String.format("Center: %.0f, %.0f, %.0f  (set to my position)", AreaClick.centerX, AreaClick.centerY, AreaClick.centerZ)
                : "Center: not set  (set to my position)"));
        keyBtn.setMessage(Component.literal(waitingForKey ? "> Press any key... <"
                : "Key: " + AreaClick.keyName()));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (waitingForKey) {
            if (key != GLFW.GLFW_KEY_ESCAPE) AreaClick.keyCode = key;
            waitingForKey = false;
            AreaClick.resetState();
            refresh();
            return true;
        }
        boolean typing = getFocused() instanceof EditBox;
        if (key == GLFW.GLFW_KEY_M && !typing) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int y = this.height / 2 - 80;
        g.drawCenteredString(this.font, this.title, cx, y - 20, 0xFFFFFFFF);
        g.drawString(this.font, "Width X", cx - 100, y + 29, 0xFFAAAAAA);
        g.drawString(this.font, "Depth Z", cx - 31, y + 29, 0xFFAAAAAA);
        g.drawString(this.font, "Height Y", cx + 38, y + 29, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
