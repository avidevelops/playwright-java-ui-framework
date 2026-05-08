package com.example.ui.infrastructure;

/**
 * Execution mode for UI tests.
 * <p>
 * Driven by the {@code qa.ui.mode} property in {@code application.properties}.
 * <ul>
 *   <li>{@code MOCK} — Playwright intercepts every API call via {@code page.route()};
 *       no real backend is required.</li>
 *   <li>{@code LIVE} — Real HTTP requests are made; requires a running backend.</li>
 * </ul>
 * </p>
 *
 * <p>Note: there is no {@code @mock} Cucumber tag. The mode is an infrastructure concern
 * controlled entirely by this property. The only meaningful test tag is {@code @live},
 * which marks scenarios whose assertions are safe regardless of what the real backend returns.</p>
 */
public enum MockMode {

    MOCK, LIVE;

    public static MockMode resolve() {
        String mode = PropertyLoader.get("qa.ui.mode", "mock");
        return "mock".equalsIgnoreCase(mode.trim()) ? MOCK : LIVE;
    }

    /** @return true when running with Playwright network interception active. */
    public boolean isMocked() { return this == MOCK; }

    /** @return true when making real HTTP requests to the backend. */
    public boolean isLive()   { return this == LIVE; }
}
