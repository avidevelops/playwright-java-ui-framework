package com.example.ui.backend.articles;

import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.backend.articles.models.ArticleDetailResponseModel;
import com.example.ui.backend.articles.models.ArticleListResponseModel;
import com.example.ui.backend.articles.models.ArticleModel;
import com.example.ui.infrastructure.RequestMocker;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Registers Playwright route intercepts for the Conduit Articles API.
 * <p>
 * One method per endpoint. Each method:
 * <ol>
 *   <li>Calls {@link RequestMocker} to register a {@code page.route()} intercept</li>
 *   <li>Returns the alias string that can be used to inspect the captured request later</li>
 * </ol>
 * </p>
 *
 * <h3>Endpoints covered:</h3>
 * <ul>
 *   <li>{@code GET /api/articles}         — article list / feed</li>
 *   <li>{@code GET /api/articles/{slug}}  — single article detail</li>
 * </ul>
 */
@Slf4j
public class ArticlesMockApi {

    private static final String API_PREFIX = "/api";

    private final RequestMocker mocker;

    public ArticlesMockApi(RequestMocker mocker) {
        this.mocker = mocker;
    }

    // ── Article feed ─────────────────────────────────────────────────────────

    /**
     * Mocks {@code GET /api/articles} (and any query-string variants).
     * The {@code **} suffix in the glob pattern matches {@code ?limit=10&offset=0} etc.
     */
    public String mockGetArticles(ArticleListResponseModel response) {
        return mocker
                .to(API_PREFIX + "/articles")
                .withStatus(200)
                .as("getArticles")
                .get(response);
    }

    // ── Article detail ────────────────────────────────────────────────────────

    /**
     * Mocks {@code GET /api/articles/{slug}}.
     * The slug is embedded in the URL pattern so each article gets its own intercept.
     */
    public String mockGetArticle(ArticleModel article) {
        return mocker
                .to(API_PREFIX + "/articles/" + article.slug())
                .withStatus(200)
                .as("getArticle-" + article.slug())
                .get(new ArticleDetailResponseModel(article));
    }

    // ── Defaults ─────────────────────────────────────────────────────────────

    /**
     * Registers sensible defaults for all endpoints.
     * Visitors call this when no generator overrides have been provided.
     */
    public void mockAllDefaults() {
        List<ArticleModel> articles = IntStream.range(0, 10)
                .mapToObj(i -> ArticleGenerator.create().build())
                .toList();
        mockGetArticles(new ArticleListResponseModel(articles, articles.size()));
        // Also mock the first article's detail endpoint in case the test navigates to it.
        mockGetArticle(articles.get(0));
        log.debug("[MOCK API] Article defaults registered ({} articles)", articles.size());
    }
}
