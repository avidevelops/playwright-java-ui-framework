package com.example.ui.visitors;

import com.example.ui.backend.articles.ArticlesMockApi;
import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.backend.articles.models.ArticleModel;
import com.example.ui.infrastructure.AppInitMocker;
import com.example.ui.infrastructure.MockMode;
import com.example.ui.infrastructure.RequestMocker;
import com.example.ui.infrastructure.UiConstants;
import com.example.ui.pages.ArticleDetailPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;

/**
 * Visitor for the Article Detail page (route: {@code /article/{slug}}).
 * <p>
 * In mock mode, registers a {@code GET /api/articles/{slug}} intercept before navigating.
 * The article's slug is used both as the mock URL path and the navigation URL path.
 * </p>
 *
 * <h3>Usage (NavigationSteps):</h3>
 * <pre>{@code
 *   ArticleDetailVisitor visitor = ArticleDetailVisitor.create(page, mocker);
 *
 *   // Optionally override with a specific article generator:
 *   if (context.getArticleGenerator() != null) {
 *       visitor.withArticle(context.getArticleGenerator());
 *   } else {
 *       visitor.withArticle(ArticleGenerator.create().withSlug(slug));
 *   }
 *
 *   visitor.visit();
 * }</pre>
 */
@Slf4j
public class ArticleDetailVisitor implements PageVisitor {

    private final Page page;
    private final RequestMocker mocker;
    private final MockMode mode;

    // Default: a single auto-generated article.
    private ArticleGenerator articleGenerator = ArticleGenerator.create();

    private ArticleDetailVisitor(Page page, RequestMocker mocker, MockMode mode) {
        this.page = page;
        this.mocker = mocker;
        this.mode = mode;
    }

    public static ArticleDetailVisitor create(Page page, RequestMocker mocker) {
        return new ArticleDetailVisitor(page, mocker, MockMode.resolve());
    }

    /**
     * Factory with explicit mode — avoids coupling to
     * {@link com.example.ui.infrastructure.UiTestExtension} ThreadLocal.
     */
    public static ArticleDetailVisitor create(Page page, RequestMocker mocker, MockMode mode) {
        return new ArticleDetailVisitor(page, mocker, mode);
    }

    /** Override with a specific article generator. The generator’s slug drives both mock URL and navigation URL. */
    public ArticleDetailVisitor withArticle(ArticleGenerator generator) {
        this.articleGenerator = generator;
        return this;
    }

    @Override
    public void visit() {
        // Build once — slug is used for both the mock intercept path and the navigation URL.
        ArticleModel article = articleGenerator.build();

        if (mode.isMocked()) {
            AppInitMocker.mock(mocker);
            new ArticlesMockApi(mocker).mockGetArticle(article);
        }

        page.navigate(UiConstants.BASE_URL + ArticleDetailPage.pathFor(article.slug()));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.debug("[VISITOR] Article detail loaded (slug={})", article.slug());
    }

    @Override
    public String pageUrl() {
        return UiConstants.BASE_URL + ArticleDetailPage.pathFor(articleGenerator.getSlug());
    }
}
