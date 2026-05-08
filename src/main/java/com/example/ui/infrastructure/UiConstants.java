package com.example.ui.infrastructure;

/**
 * UI test configuration constants, all read from {@code application.properties} once
 * at class initialisation. Use these throughout the framework instead of reading
 * properties directly.
 */
public final class UiConstants {

    public static final String  BASE_URL;
    public static final boolean HEADLESS;
    public static final int     VIEWPORT_WIDTH;
    public static final int     VIEWPORT_HEIGHT;
    public static final String  SCREENSHOT_DIR;

    static {
        BASE_URL        = PropertyLoader.get("qa.ui.baseUrl",          "http://localhost:4100");
        HEADLESS        = PropertyLoader.getBoolean("qa.ui.headless",  true);
        VIEWPORT_WIDTH  = PropertyLoader.getInt("qa.ui.viewport.width",  1440);
        VIEWPORT_HEIGHT = PropertyLoader.getInt("qa.ui.viewport.height",  900);
        SCREENSHOT_DIR  = PropertyLoader.get("qa.ui.screenshot.dir",  "target/screenshots");
    }

    private UiConstants() {}
}
