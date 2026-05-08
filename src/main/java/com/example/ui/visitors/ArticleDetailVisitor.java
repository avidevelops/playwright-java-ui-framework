package com.example.ui.visitors;

import com.example.ui.backend.articles.ArticlesMockApi;
import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.backend.articles.models.ArticleModel;
import com.example.ui.backend.user.generators.UserGenerator;
import com.example.ui.infrastructure.AppInitMocker;
import com.example.ui.infrastructure.RequestMocker;
import com.example.ui.infrastructure.UiConstants;
import com.example.ui.infrastructure.UiTestExtension;
import com.example.ui.pages.ArticleDetailPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;

/**
 * Visitor for the Article Detail page (route: {@code /article/{slug}}).
 * <p>
 * In mock mode, registers {@link AppInitMocker} bootstrap mocks (auth + health)
 * followed by the page-specific {@code GET /api/articles/{slug}} intercept, then navigates.
 * The article's slug is used both as the mock URL path and the navigation URL path.
 * </p>
 *
 * <h3>Usage (via VisitorCommands — preferred):</h3>
 * <pre>{@code
 *   // Zero-config
 *   VisitorCommands.visitArticleDetail(page, mocker);
 *
 *   // With a specific generator
 *   VisitorCommands.visitArticleDetail(page, mocker,
 *           ArticleGenerator.create().withTitle("Custom Title"));
 *
 *   // With an already-built model
 *   VisitorCommands.visitArticleDetail(page, mocker, existingArticleModel);
 * }</pre>
 *
 * <h3>Direct usage (NavigationSteps):</h3>
 * <pre>{@code
 *   ArticleDetailVisitor visitor = ArticleDetailVisitor.create(page, mocker);
 *   if (context.getArticleGenerator() != null) {
 *       visitor.withArticle(context.getArticleGenerator());
 *   } else {
 *       visitor.withArticle(ArticleGenerator.create().withSlug(slug));
 *   }
 *   visitor.visit();
 * }</pre>
 */
@Slf4j
public class ArticleDetailVisitor implements PageVisitor {

    private final Page page;
    private final RequestMocker mocker;

    // Default: a single auto-generated article.
    private ArticleGenerator articleGenerator = ArticleGenerator.create();

    // Default: auto-generated user — overridable via withUser()
    private UserGenerator userGenerator = UserGenerator.create();

    private ArticleDetailVisitor(Page page, RequestMocker mocker) {
        this.page = page;
        this.mocker = mocker;
    }

    public static ArticleDetailVisitor create(Page page, RequestMocker mocker) {
        return new ArticleDetailVisitor(page, mocker);
    }

    /**
     * Override with a specific article generator.
     * The generator's slug drives both the mock intercept path and the navigation URL.
     *
     * @param generator a configured generator — {@code .build()} is called internally
     * @return this visitor for chaining
     */
    public ArticleDetailVisitor withArticle(ArticleGenerator generator) {
        this.articleGenerator = generator;
        return this;
    }

    /**
     * Override the authenticated user session for this page visit.
     *
     * @param userGenerator a configured {@link UserGenerator}
     * @return this visitor for chaining
     */
    public ArticleDetailVisitor withUser(UserGenerator userGenerator) {
        this.userGenerator = userGenerator;
        return this;
    }

    @Override
    public void visit() {
        // Build once — slug is used for both the mock intercept path and the navigation URL.
        ArticleModel article = articleGenerator.build();

        if (UiTestExtension.getMode().isMocked()) {
            // AppInitMocker MUST be registered before any page-specific mocks.
            // It guards against auth redirect loops and health-check timing races.
            AppInitMocker.mock(mocker, userGenerator);
            new ArticlesMockApi(mocker).mockGetArticle(article);
        }

        page.navigate(UiConstants.BASE_URL + ArticleDetailPage.pathFor(article.slug()));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.debug("[VISITOR] Article detail loaded (slug={}, user={})",
                article.slug(), userGenerator.build().username());
    }
}
