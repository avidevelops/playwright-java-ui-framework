package com.example.ui.visitors;

import com.example.ui.backend.articles.ArticlesMockApi;
import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.backend.articles.models.ArticleListResponseModel;
import com.example.ui.backend.articles.models.ArticleModel;
import com.example.ui.backend.user.generators.UserGenerator;
import com.example.ui.infrastructure.AppInitMocker;
import com.example.ui.infrastructure.RequestMocker;
import com.example.ui.infrastructure.UiConstants;
import com.example.ui.infrastructure.UiTestExtension;
import com.example.ui.pages.ArticleFeedPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Visitor for the Article Feed page (route: {@code /}).
 * <p>
 * In mock mode, registers {@link AppInitMocker} bootstrap mocks (auth + health)
 * followed by the page-specific {@code GET /api/articles} intercept, then navigates.
 * In live mode, navigates directly — no interception.
 * </p>
 *
 * <h3>Usage (via VisitorCommands — preferred):</h3>
 * <pre>{@code
 *   // Zero-config
 *   VisitorCommands.visitArticleFeed(page, mocker);
 *
 *   // With specific articles
 *   VisitorCommands.visitArticleFeed(page, mocker, myArticles);
 *
 *   // With custom user session
 *   VisitorCommands.visitArticleFeed(page, mocker,
 *           UserGenerator.create().withUsername("admin"));
 * }</pre>
 *
 * <h3>Direct usage (NavigationSteps):</h3>
 * <pre>{@code
 *   ArticleFeedVisitor visitor = ArticleFeedVisitor.create(page, mocker);
 *   if (!context.getArticles().isEmpty()) visitor.withArticles(context.getArticles());
 *   visitor.visit();
 * }</pre>
 */
@Slf4j
public class ArticleFeedVisitor implements PageVisitor {

    private final Page page;
    private final RequestMocker mocker;

    // Default: 10 auto-generated articles — works for zero-config scenarios.
    private List<ArticleModel> articles;

    // Default: auto-generated user — overridable via withUser()
    private UserGenerator userGenerator = UserGenerator.create();

    private ArticleFeedVisitor(Page page, RequestMocker mocker) {
        this.page = page;
        this.mocker = mocker;
        this.articles = IntStream.range(0, 10)
                .mapToObj(i -> ArticleGenerator.create().build())
                .toList();
    }

    public static ArticleFeedVisitor create(Page page, RequestMocker mocker) {
        return new ArticleFeedVisitor(page, mocker);
    }

    /** Override the article list. Only call this if the scenario needs specific articles. */
    public ArticleFeedVisitor withArticles(List<ArticleModel> articles) {
        this.articles = articles;
        return this;
    }

    /**
     * Override the authenticated user session for this page visit.
     * <p>
     * Use when the feed content or UI state depends on user identity
     * (e.g. personalised feed, admin toolbar, username displayed in header).
     * The provided generator is passed to {@link AppInitMocker} which mocks
     * {@code GET /api/user} with the generated user before navigation.
     * </p>
     *
     * @param userGenerator a configured {@link UserGenerator} — {@code .build()} called internally
     * @return this visitor for chaining
     */
    public ArticleFeedVisitor withUser(UserGenerator userGenerator) {
        this.userGenerator = userGenerator;
        return this;
    }

    @Override
    public void visit() {
        if (UiTestExtension.getMode().isMocked()) {
            // AppInitMocker MUST be registered before any page-specific mocks.
            // It guards against auth redirect loops and health-check timing races.
            AppInitMocker.mock(mocker, userGenerator);
            registerMocks();
        }
        page.navigate(UiConstants.BASE_URL + ArticleFeedPage.PATH);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.debug("[VISITOR] Article feed loaded ({} articles mocked, user={})",
                articles.size(), userGenerator.build().username());
    }

    private void registerMocks() {
        new ArticlesMockApi(mocker)
                .mockGetArticles(new ArticleListResponseModel(articles, articles.size()));
    }
}
