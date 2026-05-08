package com.example.ui.support;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Deferred wait utilities for asserting on requests triggered by user actions.
 * <p>
 * This is the Java equivalent of the Cypress subscriber pattern:
 * </p>
 * <pre>{@code
 *   // Cypress:
 *   const waitRMGs = subscribeToGetRmgListByOptionId([group], 1);
 *   clickSomeButton();
 *   waitRMGs(); // blocks until the request fires AFTER the click
 * }</pre>
 *
 * <pre>{@code
 *   // Java equivalent:
 *   Supplier<Response> waitFavorite = Subscribers.onArticleFavorite(page, "my-slug");
 *   articlePage.clickFavoriteButton();          // triggers the POST
 *   Response response = waitFavorite.get();     // blocks until received
 *   assertThat(response.status()).isEqualTo(200);
 * }</pre>
 *
 * <h3>How it works:</h3>
 * <ol>
 *   <li>A {@code page.onResponse()} listener is registered BEFORE the action.</li>
 *   <li>The action triggers the HTTP request.</li>
 *   <li>Calling {@code .get()} on the returned {@link Supplier} blocks until the matching
 *       response arrives, then returns it. Throws {@link AssertionError} on timeout.</li>
 * </ol>
 *
 * <h3>Thread safety:</h3>
 * Uses {@link CompletableFuture} — safe for Playwright's single-threaded page model
 * because the listener callback fires on the Playwright thread and {@code .get()} blocks
 * on the test thread.
 */
@Slf4j
public final class Subscribers {

    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    private Subscribers() {}

    // ── Article interactions ──────────────────────────────────────────────────

    /**
     * Subscribes to the {@code POST /api/articles/{slug}/favorite} response.
     * Register before clicking the favorite button; call {@code .get()} after.
     *
     * @param page the Playwright page
     * @param slug the article slug whose favorite endpoint is expected
     * @return a {@link Supplier} that blocks until the response arrives
     */
    public static Supplier<Response> onArticleFavorite(Page page, String slug) {
        return subscribe(
                page,
                resp -> resp.url().contains("/api/articles/" + slug + "/favorite")
                        && resp.request().method().equalsIgnoreCase("POST"),
                "article favorite (slug=" + slug + ")"
        );
    }

    /**
     * Subscribes to the {@code POST /api/articles} response (article creation).
     * Register before submitting the create-article form; call {@code .get()} after.
     *
     * @param page the Playwright page
     * @return a {@link Supplier} that blocks until the response arrives
     */
    public static Supplier<Response> onArticleCreate(Page page) {
        return subscribe(
                page,
                resp -> {
                    String url = resp.url();
                    // Match POST /api/articles but NOT /api/articles/{slug}/... sub-paths
                    return url.matches(".*/api/articles/?$")
                            && resp.request().method().equalsIgnoreCase("POST");
                },
                "article create"
        );
    }

    /**
     * Subscribes to the {@code PUT /api/articles/{slug}} response (article update).
     * Register before submitting the edit-article form; call {@code .get()} after.
     *
     * @param page the Playwright page
     * @param slug the article slug being updated
     * @return a {@link Supplier} that blocks until the response arrives
     */
    public static Supplier<Response> onArticleUpdate(Page page, String slug) {
        return subscribe(
                page,
                resp -> resp.url().contains("/api/articles/" + slug)
                        && resp.request().method().equalsIgnoreCase("PUT"),
                "article update (slug=" + slug + ")"
        );
    }

    /**
     * Subscribes to the {@code DELETE /api/articles/{slug}} response (article deletion).
     * Register before clicking delete; call {@code .get()} after.
     *
     * @param page the Playwright page
     * @param slug the article slug being deleted
     * @return a {@link Supplier} that blocks until the response arrives
     */
    public static Supplier<Response> onArticleDelete(Page page, String slug) {
        return subscribe(
                page,
                resp -> resp.url().contains("/api/articles/" + slug)
                        && resp.request().method().equalsIgnoreCase("DELETE"),
                "article delete (slug=" + slug + ")"
        );
    }

    // ── Generic ───────────────────────────────────────────────────────────────

    /**
     * Creates a custom subscriber for any response matching the given predicate.
     * <p>
     * Use when none of the pre-built methods match your use case.
     * </p>
     *
     * <pre>{@code
     *   Supplier<Response> wait = Subscribers.on(page,
     *       resp -> resp.url().contains("/api/comments") && resp.status() == 201,
     *       "comment create");
     *   submitButton.click();
     *   Response resp = wait.get();
     * }</pre>
     *
     * @param page        the Playwright page
     * @param matcher     predicate evaluated against each response as it arrives
     * @param description human-readable label used in the timeout error message
     * @return a {@link Supplier} that blocks until a matching response arrives
     */
    public static Supplier<Response> on(Page page, Predicate<Response> matcher, String description) {
        return subscribe(page, matcher, description);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private static Supplier<Response> subscribe(Page page, Predicate<Response> matcher, String description) {
        CompletableFuture<Response> future = new CompletableFuture<>();

        page.onResponse(resp -> {
            if (!future.isDone()) {
                try {
                    if (matcher.test(resp)) {
                        log.debug("[SUBSCRIBER] Matched response for '{}': {} {}",
                                description, resp.request().method(), resp.url());
                        future.complete(resp);
                    }
                } catch (Exception e) {
                    // Ignore — predicate may throw if request body is unavailable
                }
            }
        });

        return () -> {
            try {
                return future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                throw new AssertionError(
                        "[SUBSCRIBER] No response for [" + description + "] within "
                                + DEFAULT_TIMEOUT_SECONDS + "s. Did the action trigger the expected request?",
                        e);
            } catch (Exception e) {
                throw new AssertionError(
                        "[SUBSCRIBER] Failed waiting for [" + description + "]", e);
            }
        };
    }
}
