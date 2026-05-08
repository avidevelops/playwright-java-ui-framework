package com.example.ui.visitors;

import com.example.ui.backend.articles.ArticlesMockApi;
import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.backend.articles.models.ArticleListResponseModel;
import com.example.ui.backend.articles.models.ArticleModel;
import com.example.ui.infrastructure.AppInitMocker;
import com.example.ui.infrastructure.MockMode;
import com.example.ui.infrastructure.RequestMocker;
import com.example.ui.infrastructure.UiConstants;
import com.example.ui.pages.ArticleFeedPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Visitor for the Article Feed page (route: {@code /}).
 * <p>
 * In mock mode, registers a {@code GET /api/articles} intercept before navigating.
 * In live mode, navigates directly — no interception.
 * </p>
 *
 * <h3>Usage (NavigationSteps):</h3>
 * <pre>{@code
 *   ArticleFeedVisitor visitor = ArticleFeedVisitor.create(page, mocker);
 *
 *   // Optionally override the default article list:
 *   if (!context.getArticles().isEmpty()) {
 *       visitor.withArticles(context.getArticles());
 *   }
 *
 *   visitor.visit();
 * }</pre>
 */
@Slf4j
public class ArticleFeedVisitor implements PageVisitor {

    private final Page page;
    private final RequestMocker mocker;
    private final MockMode mode;

    // Default: 10 auto-generated articles — works for zero-config scenarios.
    private List<ArticleModel> articles;

    private ArticleFeedVisitor(Page page, RequestMocker mocker, MockMode mode) {
        this.page = page;
        this.mocker = mocker;
        this.mode = mode;
        this.articles = IntStream.range(0, 10)
                .mapToObj(i -> ArticleGenerator.create().build())
                .toList();
    }

    public static ArticleFeedVisitor create(Page page, RequestMocker mocker) {
        return new ArticleFeedVisitor(page, mocker, MockMode.resolve());
    }

    /**
     * Factory with explicit mode — useful for unit testing visitors without a running
     * Playwright context (avoids the {@link com.example.ui.infrastructure.UiTestExtension} ThreadLocal).
     */
    public static ArticleFeedVisitor create(Page page, RequestMocker mocker, MockMode mode) {
        return new ArticleFeedVisitor(page, mocker, mode);
    }

    /** Override the article list. Only call this if the scenario needs specific articles. */
    public ArticleFeedVisitor withArticles(List<ArticleModel> articles) {
        this.articles = articles;
        return this;
    }

    @Override
    public void visit() {
        if (mode.isMocked()) {
            registerMocks();
        }
        page.navigate(pageUrl());
        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.debug("[VISITOR] Article feed loaded ({} articles mocked)", articles.size());
    }

    @Override
    public String pageUrl() {
        return UiConstants.BASE_URL + ArticleFeedPage.PATH;
    }

    private void registerMocks() {
        AppInitMocker.mock(mocker);
        new ArticlesMockApi(mocker)
                .mockGetArticles(new ArticleListResponseModel(articles, articles.size()));
    }
}
