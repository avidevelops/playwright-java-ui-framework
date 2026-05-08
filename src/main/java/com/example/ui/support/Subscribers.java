package com.example.ui.support;

import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.backend.articles.models.ArticleDetailResponseModel;
import com.example.ui.backend.articles.models.ArticleListResponseModel;
import com.example.ui.backend.articles.models.ArticleModel;
import com.example.ui.infrastructure.RequestMocker;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

/**
 * Deferred wait-handle factory — the Playwright Java equivalent of the Cypress
 * subscriber pattern ({@code () => cy.wait('@alias')}).
 *
 * <h2>Why this pattern exists</h2>
 * <p>
 * Testing <em>async UI behaviour</em> (e.g. “clicking Submit fires a POST with the
 * right body”) requires:
 * <ol>
 *   <li><strong>Register the mock intercept</strong> — <em>before</em> the UI action</li>
 *   <li><strong>Trigger the UI action</strong> (click, form submit, keyboard shortcut…)</li>
 *   <li><strong>Wait for the request/response</strong> and assert on it</li>
 * </ol>
 * Without this pattern tests either use {@code Thread.sleep()} (flaky) or miss the
 * request entirely because the intercept is registered after it fires.
 * </p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *   // 1. Get a wait-handle BEFORE the action
 *   Supplier<Response> waitCreate = Subscribers.onArticleCreate(page, mocker, newArticle);
 *
 *   // 2. Trigger the UI action
 *   new ArticleEditorPage(page).submitButton().click();
 *
 *   // 3. Block until the request fires and assert on the response
 *   Response response = waitCreate.get();
 *   assertThat(response.status()).isEqualTo(201);
 *
 *   // 4. Optionally inspect the request body the frontend sent
 *   RequestMocker.InterceptedRequest captured = mocker.getIntercepted("createArticle");
 *   assertThat(captured.requestBody()).contains("\"title\":\"My New Article\"");
 * }</pre>
 *
 * <h2>How it works</h2>
 * <p>
 * Each factory method:
 * <ol>
 *   <li>Registers a mock intercept via {@link RequestMocker} so the response is
 *       controlled (no real backend needed)</li>
 *   <li>Returns a {@code Supplier<Response>} that, when {@code .get()} is called,
 *       invokes {@link Page#waitForResponse(String, Runnable)} with a
 *       <strong>no-op action lambda</strong> — the real UI action was already triggered
 *       by the caller between steps 1 and 3.</li>
 * </ol>
 * </p>
 *
 * <h2>Extending for your domain</h2>
 * <p>
 * Add a new static method per POST/PUT/DELETE endpoint you want to assert on:
 * </p>
 * <pre>{@code
 *   public static Supplier<Response> onOrderCreate(Page page, RequestMocker mocker, OrderModel order) {
 *       mocker.to("/api/orders").withStatus(201).as("createOrder").post(new OrderResponseModel(order));
 *       return () -> page.waitForResponse("**/api/orders", () -> {});
 *   }
 * }</pre>
 */
@Slf4j
public final class Subscribers {

    private Subscribers() {}

    // ── Article endpoints ───────────────────────────────────────────────────

    /**
     * Registers a mock for {@code POST /api/articles} and returns a wait-handle.
     * <p>
     * Call this <em>before</em> the UI action that triggers the create request.
     * Call {@code .get()} <em>after</em> the UI action to block until the
     * response is received.
     * </p>
     *
     * @param page    the Playwright {@link Page} for the current scenario
     * @param mocker  the per-scenario {@link RequestMocker}
     * @param article the article that should be returned as the mock response
     * @return a {@code Supplier<Response>} — call {@code .get()} after triggering the UI action
     */
    public static Supplier<Response> onArticleCreate(
            Page page,
            RequestMocker mocker,
            ArticleModel article
    ) {
        mocker
                .to("/api/articles")
                .withStatus(201)
                .as("createArticle")
                .post(new ArticleDetailResponseModel(article));

        log.debug("[SUBSCRIBER] Registered mock for POST /api/articles (alias=createArticle)");

        return () -> {
            log.debug("[SUBSCRIBER] Waiting for POST /api/articles response...");
            Response response = page.waitForResponse(
                    resp -> resp.url().contains("/api/articles")
                            && resp.request().method().equals("POST"),
                    () -> {} // action already triggered by caller
            );
            log.debug("[SUBSCRIBER] POST /api/articles responded (status={})", response.status());
            return response;
        };
    }

    /**
     * Registers a mock for {@code PUT /api/articles/{slug}} and returns a wait-handle.
     * Use for testing the “Edit Article” save action.
     *
     * @param page    the Playwright {@link Page} for the current scenario
     * @param mocker  the per-scenario {@link RequestMocker}
     * @param article the updated article returned as the mock response
     * @return a {@code Supplier<Response>} — call {@code .get()} after the save click
     */
    public static Supplier<Response> onArticleUpdate(
            Page page,
            RequestMocker mocker,
            ArticleModel article
    ) {
        mocker
                .to("/api/articles/" + article.slug())
                .withStatus(200)
                .as("updateArticle-" + article.slug())
                .put(new ArticleDetailResponseModel(article));

        log.debug("[SUBSCRIBER] Registered mock for PUT /api/articles/{} (alias=updateArticle-{})",
                article.slug(), article.slug());

        return () -> {
            log.debug("[SUBSCRIBER] Waiting for PUT /api/articles/{} response...", article.slug());
            Response response = page.waitForResponse(
                    resp -> resp.url().contains("/api/articles/" + article.slug())
                            && resp.request().method().equals("PUT"),
                    () -> {}
            );
            log.debug("[SUBSCRIBER] PUT /api/articles/{} responded (status={})",
                    article.slug(), response.status());
            return response;
        };
    }

    /**
     * Registers a mock for {@code DELETE /api/articles/{slug}} and returns a wait-handle.
     * Use for testing the “Delete Article” action.
     *
     * @param page   the Playwright {@link Page} for the current scenario
     * @param mocker the per-scenario {@link RequestMocker}
     * @param slug   the slug of the article being deleted
     * @return a {@code Supplier<Response>} — call {@code .get()} after the delete click
     */
    public static Supplier<Response> onArticleDelete(
            Page page,
            RequestMocker mocker,
            String slug
    ) {
        mocker
                .to("/api/articles/" + slug)
                .withStatus(204)
                .as("deleteArticle-" + slug)
                .delete();

        log.debug("[SUBSCRIBER] Registered mock for DELETE /api/articles/{} (alias=deleteArticle-{})",
                slug, slug);

        return () -> {
            log.debug("[SUBSCRIBER] Waiting for DELETE /api/articles/{} response...", slug);
            Response response = page.waitForResponse(
                    resp -> resp.url().contains("/api/articles/" + slug)
                            && resp.request().method().equals("DELETE"),
                    () -> {}
            );
            log.debug("[SUBSCRIBER] DELETE /api/articles/{} responded (status={})", slug, response.status());
            return response;
        };
    }

    /**
     * Registers a mock for {@code POST /api/articles/{slug}/favorite} and returns a wait-handle.
     * Use for testing the “Favorite Article” toggle action.
     *
     * @param page    the Playwright {@link Page} for the current scenario
     * @param mocker  the per-scenario {@link RequestMocker}
     * @param article the article with updated favorites count returned as the mock response
     * @return a {@code Supplier<Response>} — call {@code .get()} after the favorite button click
     */
    public static Supplier<Response> onArticleFavorite(
            Page page,
            RequestMocker mocker,
            ArticleModel article
    ) {
        mocker
                .to("/api/articles/" + article.slug() + "/favorite")
                .withStatus(200)
                .as("favoriteArticle-" + article.slug())
                .post(new ArticleDetailResponseModel(article));

        log.debug("[SUBSCRIBER] Registered mock for POST /api/articles/{}/favorite", article.slug());

        return () -> {
            log.debug("[SUBSCRIBER] Waiting for favorite toggle on {}...", article.slug());
            Response response = page.waitForResponse(
                    resp -> resp.url().contains("/api/articles/" + article.slug() + "/favorite")
                            && resp.request().method().equals("POST"),
                    () -> {}
            );
            log.debug("[SUBSCRIBER] Favorite toggle responded (status={})", response.status());
            return response;
        };
    }
}
