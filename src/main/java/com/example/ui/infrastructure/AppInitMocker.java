package com.example.ui.infrastructure;

import java.util.List;
import java.util.Map;

/**
 * Centralised registrar for app-level bootstrap mocks.
 * <p>
 * Every Visitor's {@code registerMocks()} method calls {@link #mock(RequestMocker)} as its
 * <b>first</b> step, before registering any page-specific routes.
 * </p>
 *
 * <p>This mirrors the Cypress framework's {@code cy.mockAppInitialization()} / {@code stablePageVisit}
 * pattern, where every navigation first mocks the health-check and current-user endpoints so
 * the app can bootstrap without hitting a real backend.</p>
 *
 * <h3>Conduit (RealWorld) bootstrap endpoints mocked here:</h3>
 * <ul>
 *   <li>{@code GET /api/user}  — returns the authenticated test user profile</li>
 *   <li>{@code GET /api/tags}  — returns the popular tags sidebar list</li>
 * </ul>
 *
 * <h3>Usage (inside a Visitor):</h3>
 * <pre>{@code
 *   private void registerMocks() {
 *       AppInitMocker.mock(mocker);          // always first
 *       new ArticlesMockApi(mocker).mockGetArticles(response);
 *   }
 * }</pre>
 */
public final class AppInitMocker {

    /** Alias for the current-user bootstrap intercept (use with {@code waitForAlias}). */
    public static final String ALIAS_CURRENT_USER = "getCurrentUser";

    /** Alias for the tags bootstrap intercept. */
    public static final String ALIAS_TAGS = "getTags";

    private AppInitMocker() {}

    /**
     * Registers mocks for all app bootstrap endpoints that the Conduit frontend
     * calls on every page load, regardless of which page is being visited.
     *
     * @param mocker the {@link RequestMocker} for the current scenario
     */
    public static void mock(RequestMocker mocker) {
        // Current authenticated user — app reads this on startup to show the nav bar.
        mocker.to("/api/user")
                .withStatus(200)
                .as(ALIAS_CURRENT_USER)
                .get(currentUserResponse());

        // Popular tags — rendered in the sidebar on every feed page.
        mocker.to("/api/tags")
                .withStatus(200)
                .as(ALIAS_TAGS)
                .get(tagsResponse());
    }

    // ── Response payloads ────────────────────────────────────────────────────

    private static Map<String, Object> currentUserResponse() {
        return Map.of("user", Map.of(
                "email",    "test@example.com",
                "token",    "test-jwt-token",
                "username", "test-user",
                "bio",      "Automated test user",
                "image",    ""
        ));
    }

    private static Map<String, Object> tagsResponse() {
        return Map.of("tags", List.of("automation", "testing", "playwright", "java", "cucumber"));
    }
}
