package com.example.ui.infrastructure;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages the Playwright {@link Playwright} instance and {@link Browser} singleton.
 * <p>
 * The browser is shared across all scenarios in the same JVM process. Each scenario
 * gets its own {@link com.microsoft.playwright.BrowserContext} and
 * {@link com.microsoft.playwright.Page} (managed by {@link UiTestExtension}).
 * </p>
 * <p>
 * A JVM shutdown hook closes the browser and Playwright automatically at the end
 * of the test run.
 * </p>
 */
@Slf4j
public final class PlaywrightProvider {

    private static Playwright playwright;
    private static Browser browser;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(PlaywrightProvider::close, "playwright-shutdown"));
    }

    private PlaywrightProvider() {}

    /**
     * Returns (or lazily launches) a Chromium browser instance.
     * Thread-safe: synchronised on the class.
     */
    public static synchronized Browser launchChromium() {
        if (browser == null || !browser.isConnected()) {
            log.info("[PLAYWRIGHT] Launching Chromium (headless={})", UiConstants.HEADLESS);
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(UiConstants.HEADLESS)
            );
        }
        return browser;
    }

    public static synchronized void close() {
        if (browser != null)    { try { browser.close();    } catch (Exception ignored) {} browser = null; }
        if (playwright != null) { try { playwright.close(); } catch (Exception ignored) {} playwright = null; }
        log.info("[PLAYWRIGHT] Browser closed.");
    }
}
