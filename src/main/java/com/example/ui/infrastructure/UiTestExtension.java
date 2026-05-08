package com.example.ui.infrastructure;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;

/**
 * Manages per-scenario Playwright resources: {@link BrowserContext}, {@link Page},
 * {@link RequestMocker}, and {@link MockMode}.
 * <p>
 * Resources are stored in {@link ThreadLocal}s so parallel scenario execution is safe.
 * The browser singleton (from {@link PlaywrightProvider}) is shared across all scenarios.
 * </p>
 *
 * <h3>Lifecycle:</h3>
 * <ol>
 *   <li>{@link #beforeScenario()} — called from {@code UiHooks @Before("@ui")}</li>
 *   <li>Scenario runs — step defs access page/mocker/mode via static getters</li>
 *   <li>{@link #afterScenario(boolean, String)} — called from {@code UiHooks @After("@ui")};
 *       captures screenshot on failure, closes page and context.</li>
 * </ol>
 */
@Slf4j
public final class UiTestExtension {

    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page>           PAGE    = new ThreadLocal<>();
    private static final ThreadLocal<RequestMocker>  MOCKER  = new ThreadLocal<>();
    private static final ThreadLocal<MockMode>       MODE    = new ThreadLocal<>();

    private UiTestExtension() {}

    // ── Lifecycle ───────────────────────────────────────────────────────────

    public static void beforeScenario() {
        MockMode mode = MockMode.resolve();
        Browser browser = PlaywrightProvider.launchChromium();

        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(UiConstants.VIEWPORT_WIDTH, UiConstants.VIEWPORT_HEIGHT)
        );
        Page page = context.newPage();

        CONTEXT.set(context);
        PAGE.set(page);
        MOCKER.set(new RequestMocker(page));
        MODE.set(mode);

        log.info("[UI] Scenario started (mode={})", mode);
    }

    public static void afterScenario(boolean failed, String scenarioName) {
        Page page = PAGE.get();

        if (failed && page != null) {
            try {
                File screenshotDir = new File(UiConstants.SCREENSHOT_DIR);
                screenshotDir.mkdirs();
                String safeName = scenarioName.replaceAll("[^a-zA-Z0-9_-]", "_");
                String filePath = UiConstants.SCREENSHOT_DIR + "/" + safeName + ".png";
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filePath)));
                log.info("[UI] Failure screenshot saved: {}", filePath);
            } catch (Exception e) {
                log.warn("[UI] Could not capture screenshot: {}", e.getMessage());
            }
        }

        if (page != null)           { try { page.close();    } catch (Exception ignored) {} }
        BrowserContext ctx = CONTEXT.get();
        if (ctx != null)            { try { ctx.close();     } catch (Exception ignored) {} }

        PAGE.remove();
        CONTEXT.remove();
        MOCKER.remove();
        MODE.remove();

        log.info("[UI] Scenario finished (failed={})", failed);
    }

    // ── Accessors ───────────────────────────────────────────────────────────

    /** The Playwright {@link Page} for the current scenario. */
    public static Page getPage()           { return PAGE.get(); }

    /** The {@link RequestMocker} for the current scenario (registers route intercepts). */
    public static RequestMocker getMocker() { return MOCKER.get(); }

    /** Whether the current scenario is running in mock or live mode. */
    public static MockMode getMode()        { return MODE.get(); }

    /** The {@link BrowserContext} for the current scenario. */
    public static BrowserContext getContext() { return CONTEXT.get(); }
}
