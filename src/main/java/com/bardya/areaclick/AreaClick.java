package com.bardya.areaclick;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** وقتی بازیکنی وارد محدوده میشه یک بار، و وقتی خارج میشه یک بار دیگه کلید انتخاب‌شده زده میشه. */
public final class AreaClick {
    // ---- تنظیمات ----
    public static boolean enabled = false;
    public static int width = 20;   // اندازه روی محور X
    public static int depth = 20;   // اندازه روی محور Z
    public static int height = 20;  // اندازه روی محور Y (۰ = ارتفاع چک نشه)
    public static boolean centerSet = false;
    public static double centerX, centerY, centerZ;
    public static int keyCode = -1;

    // ---- وضعیت داخلی ----
    private static boolean menuKeyDown = false;
    private static boolean wasInside = false;
    private static boolean initialized = false;
    private static boolean keyHeld = false;

    private AreaClick() {}

    private static InputConstants.Key key() {
        return InputConstants.Type.KEYSYM.getOrCreate(keyCode);
    }

    public static String keyName() {
        return keyCode < 0 ? "-" : key().getDisplayName().getString();
    }

    /** بعد از روشن کردن، وضعیت اولیه دوباره خونده میشه تا فوراً کلیدی زده نشه. */
    public static void resetState() {
        initialized = false;
        releaseKey();
    }

    private static void releaseKey() {
        if (keyHeld && keyCode >= 0) {
            KeyMapping.set(key(), false);
        }
        keyHeld = false;
    }

    private static void pressOnce() {
        KeyMapping.click(key());
        KeyMapping.set(key(), true);
        keyHeld = true; // تیک بعدی رها میشه
    }

    public static void tick(Minecraft mc) {
        long window = mc.getWindow().handle();

        // ---- باز کردن منو با M ----
        boolean m = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_M) == GLFW.GLFW_PRESS;
        if (m && !menuKeyDown && mc.screen == null && mc.player != null) {
            mc.setScreen(new AreaClickScreen());
        }
        menuKeyDown = m;

        // کلیدی که تیک قبل زده شد رها بشه
        if (keyHeld) releaseKey();

        if (mc.player == null || mc.level == null) {
            initialized = false;
            return;
        }
        if (!enabled || !centerSet || keyCode < 0) {
            initialized = false;
            return;
        }

        boolean inside = anyPlayerInside(mc);

        if (!initialized) {
            wasInside = inside;
            initialized = true;
            return;
        }

        if (inside != wasInside) {
            pressOnce(); // ورود یا خروج
            mc.player.displayClientMessage(Component.literal(
                    inside ? "Area Click: player ENTERED" : "Area Click: player LEFT"), true);
            wasInside = inside;
        }
    }

    private static boolean anyPlayerInside(Minecraft mc) {
        double halfW = width / 2.0;
        double halfD = depth / 2.0;
        double halfH = height / 2.0;
        for (AbstractClientPlayer p : mc.level.players()) {
            if (p == mc.player) continue;
            boolean inXZ = Math.abs(p.getX() - centerX) <= halfW && Math.abs(p.getZ() - centerZ) <= halfD;
            boolean inY = height <= 0 || Math.abs(p.getY() - centerY) <= halfH;
            if (inXZ && inY) {
                return true;
            }
        }
        return false;
    }
}
