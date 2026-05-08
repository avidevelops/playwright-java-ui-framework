package com.example.ui.support;

import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.backend.articles.models.ArticleModel;
import com.example.ui.backend.user.generators.UserGenerator;
import com.example.ui.infrastructure.RequestMocker;
import com.example.ui.visitors.ArticleDetailVisitor;
import com.example.ui.visitors.ArticleFeedVisitor;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * High-level static facade for page navigation — the Playwright Java equivalent
 * of Cypress custom commands ({@code cy.visitArticleFeed()}, {@code cy.visitArticleDetail()}).
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><strong>Zero-config defaults</strong> — every method has a no-arg overload that
 *       auto-generates all test data. A test writer can call
 *       {@code VisitorCommands.visitArticleFeed(page, mocker)} with no other setup.</li>
 *   <li><strong>Full override capability</strong> — every method has an overloaded form
 *       accepting generators/models, so a test only needs to specify what matters.</li>
 *   <li><strong>Single entry point</strong> — test code (both Cucumber steps and JUnit 5
 *       tests) never instantiates Visitor classes directly. All navigation flows through
 *       this class.</li>
 *   <li><strong>AppInitMocker always wired</strong> — visitors called from here always
 *       register auth + health-check bootstrap mocks before navigating.</li>
 * </ul>
 *
 * <h2>Usage in JUnit 5 tests</h2>
 * <pre>{@code
 * @ExtendWith(UiTestExtension.class)
 * class ArticleDetailTest {
 *
 *     @Test
 *     void titleIsDisplayed(Page page, RequestMocker mocker) {
 *         VisitorCommands.visitArticleDetail(page, mocker);
 *         assertThat(new ArticleDetailPage(page).getTitle()).isNotBlank();
 *     }
 *
 *     @Test
 *     void specificTitleIsDisplayed(Page page, RequestMocker mocker) {
 *         VisitorCommands.visitArticleDetail(page, mocker,
 *                 ArticleGenerator.create().withTitle("Test-Driven Java"));
 *         assertThat(new ArticleDetailPage(page).getTitle()).isEqualTo("Test-Driven Java");
 *     }
 *
 *     @Test
 *     void feedShowsCorrectArticleCount(Page page, RequestMocker mocker) {
 *         VisitorCommands.visitArticleFeed(page, mocker, 5);
 *         assertThat(new ArticleFeedPage(page).getArticleCount()).isEqualTo(5);
 *     }
 *
 *     @Test
 *     void specificArticlesAreShown(Page page, RequestMocker mocker) {
 *         List<ArticleModel> articles = List.of(
 *                 ArticleGenerator.create().withTitle("Article Alpha").build(),
 *                 ArticleGenerator.create().withTitle("Article Beta").build()
 *         );
 *         VisitorCommands.visitArticleFeed(page, mocker, articles);
 *         assertThat(new ArticleFeedPage(page).getTitleAtRow(0)).isEqualTo("Article Alpha");
 *     }
 *
 *     @Test
 *     void reusePrebuiltModelForBothNavigationAndAssertion(Page page, RequestMocker mocker) {
 *         // Build the model once and use it for both navigation and expected-value assertion.
 *         ArticleModel article = ArticleGenerator.create().withTitle("Reused Model").build();
 *         VisitorCommands.visitArticleDetail(page, mocker, article);
 *         assertThat(new ArticleDetailPage(page).getTitle()).isEqualTo(article.title());
 *     }
 * }
 * }</pre>
 *
 * <h2>Usage in Cucumber (via NavigationSteps)</h2>
 * <pre>{@code
 * @Given("the user is on the article feed page")
 * public void iNavigateToArticleFeed() {
 *     List<ArticleModel> articles = context.getArticles();
 *     if (articles.isEmpty()) {
 *         VisitorCommands.visitArticleFeed(page, mocker);
 *     } else {
 *         VisitorCommands.visitArticleFeed(page, mocker, articles);
 *     }
 * }
 * }</pre>
 */
@Slf4j
public final class VisitorCommands {

    private VisitorCommands() {}

    // ────────────────────────────────────────────────────────────────────
    // Article Feed
    // ────────────────────────────────────────────────────────────────────

    /**
     * Navigates to the Article Feed with 10 auto-generated articles.
     * Zero-config — no setup required. Auth bootstrap mocks are always registered.
     */
    public static void visitArticleFeed(Page page, RequestMocker mocker) {
        log.debug("[COMMANDS] visitArticleFeed (default 10 articles)");
        ArticleFeedVisitor.create(page, mocker).visit();
    }

    /**
     * Navigates to the Article Feed with {@code count} auto-generated articles.
     *
     * @param count number of articles to generate
     */
    public static void visitArticleFeed(Page page, RequestMocker mocker, int count) {
        List<ArticleModel> articles = IntStream.range(0, count)
                .mapToObj(i -> ArticleGenerator.create().build())
                .collect(Collectors.toList());
        visitArticleFeed(page, mocker, articles);
    }

    /**
     * Navigates to the Article Feed with a specific list of articles.
     * Use this when test assertions depend on exact article data.
     *
     * @param articles the exact list of articles to display in the feed
     */
    public static void visitArticleFeed(Page page, RequestMocker mocker, List<ArticleModel> articles) {
        log.debug("[COMMANDS] visitArticleFeed ({} articles)", articles.size());
        ArticleFeedVisitor.create(page, mocker)
                .withArticles(articles)
                .visit();
    }

    /**
     * Navigates to the Article Feed with a custom authenticated user session.
     * Use when the feed content or header UI depends on user identity.
     *
     * @param userGen the user generator for the current session
     */
    public static void visitArticleFeed(
            Page page,
            RequestMocker mocker,
            UserGenerator userGen
    ) {
        log.debug("[COMMANDS] visitArticleFeed (user={})", userGen.build().username());
        ArticleFeedVisitor.create(page, mocker)
                .withUser(userGen)
                .visit();
    }

    // ────────────────────────────────────────────────────────────────────
    // Article Detail
    // ────────────────────────────────────────────────────────────────────

    /**
     * Navigates to the Article Detail page with a zero-config auto-generated article.
     * Auth bootstrap mocks are always registered.
     */
    public static void visitArticleDetail(Page page, RequestMocker mocker) {
        log.debug("[COMMANDS] visitArticleDetail (default article)");
        ArticleDetailVisitor.create(page, mocker).visit();
    }

    /**
     * Navigates to the Article Detail page for a specific article generator.
     * The generator's slug drives both the mock URL and the navigation URL.
     *
     * @param articleGen a configured generator — call fluent {@code with*()} methods
     *                   before passing; {@code .build()} is called internally by the Visitor
     */
    public static void visitArticleDetail(
            Page page,
            RequestMocker mocker,
            ArticleGenerator articleGen
    ) {
        log.debug("[COMMANDS] visitArticleDetail (slug={})", articleGen.getSlug());
        ArticleDetailVisitor.create(page, mocker)
                .withArticle(articleGen)
                .visit();
    }

    /**
     * Navigates to the Article Detail page for an already-built {@link ArticleModel}.
     * <p>
     * Use when you need to reuse the same model object for both navigation and assertions
     * without calling {@code .build()} again and risking field drift.
     * </p>
     * <p>
     * Internally converts the model back to a generator via
     * {@link ArticleGenerator#fromModel(ArticleModel)} so the Visitor receives
     * the consistent generator type it expects.
     * </p>
     *
     * @param article the article to display — slug is used for URL construction
     */
    public static void visitArticleDetail(
            Page page,
            RequestMocker mocker,
            ArticleModel article
    ) {
        log.debug("[COMMANDS] visitArticleDetail (pre-built article, slug={})", article.slug());
        ArticleDetailVisitor.create(page, mocker)
                .withArticle(ArticleGenerator.fromModel(article))
                .visit();
    }
}
