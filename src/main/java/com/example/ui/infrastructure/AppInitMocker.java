package com.example.ui.infrastructure;

import com.example.ui.backend.user.generators.UserGenerator;
import com.example.ui.backend.user.models.UserResponseModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Registers app-level bootstrap intercepts that every page visit requires.
 * <p>
 * Mirrors the Cypress framework's {@code cy.mockAppInitialization()} command.
 * Must be called inside every {@link com.example.ui.visitors.PageVisitor#visit()}
 * implementation <em>before</em> {@code page.navigate()} — guards against:
 * <ul>
 *   <li>Auth redirect loops when the real backend is not running</li>
 *   <li>Config/feature-flag API timing races that cause flaky tests</li>
 *   <li>Health-check polling blocking the page from reaching NETWORKIDLE</li>
 * </ul>
 * </p>
 *
 * <h3>Endpoints mocked:</h3>
 * <ul>
 *   <li>{@code GET /api/user}   — authenticated user session (JWT, username, etc.)</li>
 *   <li>{@code GET /api/health} — application health check</li>
 * </ul>
 *
 * <h3>Usage — inside any Visitor:</h3>
 * <pre>{@code
 *   @Override
 *   public void visit() {
 *       if (UiTestExtension.getMode().isMocked()) {
 *           AppInitMocker.mock(mocker);          // ← always first
 *           AppInitMocker.mock(mocker, userGen); // ← or with a custom user
 *           registerPageSpecificMocks();
 *       }
 *       page.navigate(url);
 *       page.waitForLoadState(LoadState.NETWORKIDLE);
 *   }
 * }</pre>
 *
 * <h3>Custom user override:</h3>
 * <pre>{@code
 *   AppInitMocker.mock(mocker,
 *       UserGenerator.create().withUsername("admin-user").withToken("special-token"));
 * }</pre>
 */
@Slf4j
public final class AppInitMocker {

    /** Alias used for the current-user intercept — inspectable via {@code mocker.getIntercepted(ALIAS_USER)}. */
    public static final String ALIAS_USER   = "appInit_getCurrentUser";

    /** Alias used for the health-check intercept — inspectable via {@code mocker.getIntercepted(ALIAS_HEALTH)}. */
    public static final String ALIAS_HEALTH = "appInit_healthCheck";

    private AppInitMocker() {}

    /**
     * Registers bootstrap mocks using a default auto-generated user.
     *
     * @param mocker the per-scenario {@link RequestMocker}
     */
    public static void mock(RequestMocker mocker) {
        mock(mocker, UserGenerator.create());
    }

    /**
     * Registers bootstrap mocks using a custom user generator.
     * Call this when a test needs a specific username, token, or role.
     *
     * @param mocker  the per-scenario {@link RequestMocker}
     * @param userGen a configured {@link UserGenerator} — {@code .build()} is called internally
     */
    public static void mock(RequestMocker mocker, UserGenerator userGen) {
        mocker
                .to("/api/user")
                .withStatus(200)
                .as(ALIAS_USER)
                .get(new UserResponseModel(userGen.build()));

        mocker
                .to("/api/health")
                .withStatus(200)
                .as(ALIAS_HEALTH)
                .get(Map.of("status", "UP", "timestamp", java.time.Instant.now().toString()));

        log.debug("[APP INIT] Bootstrap mocks registered (user={}, health=UP)",
                userGen.build().username());
    }
}
