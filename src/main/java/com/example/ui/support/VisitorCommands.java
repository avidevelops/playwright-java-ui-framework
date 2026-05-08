package com.example.ui.support;

import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.backend.articles.models.ArticleModel;
import com.example.ui.infrastructure.MockMode;
import com.example.ui.infrastructure.RequestMocker;
import com.example.ui.visitors.ArticleDetailVisitor;
import com.example.ui.visitors.ArticleFeedVisitor;
import com.microsoft.playwright.Page;

import java.util.List;

/**
 * High-level, one-liner navigation commands for use outside Cucumber step definitions.
 * <p>
 * This is the Java equivalent of the Cypress custom commands registered in
 * {@code cypress/support/commands/visitors.ts} — specifically {@code cy.visitDeal()},
 * {@code cy.visitRiskDescriptor()}, etc. Every method:
 * <ol>
 *   <li>Creates the appropriate Visitor (with smart defaults)</li>
 *   <li>Applies any optional overrides</li>
 *   <li>Calls {@code visitor.visit()} — which registers mocks and navigates</li>
 * </ol>
 * </p>
 *
 * <h3>When to use vs. Cucumber steps:</h3>
 * <ul>
 *   <li>Cucumber steps ({@code NavigationSteps}) are the primary entry point for BDD scenarios.</li>
 *   <li>{@code VisitorCommands} is for JUnit 5 direct tests, utility methods, or when you want
 *       to express navigation as a single Java method call without Gherkin overhead.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 *   // Zero-config — defaults handle everything
 *   VisitorCommands.visitArticleFeed(page, mocker);
 *
 *   // Override with specific articles
 *   VisitorCommands.visitArticleFeed(page, mocker, List.of(
 *       ArticleGenerator.create().withTitle("My Test Article").build()
 *   ));
 *
 *   // Navigate to a specific article detail page
 *   VisitorCommands.visitArticleDetail(page, mocker, "my-article-slug");
 *
 *   // Full override via generator
 *   ArticleGenerator gen = ArticleGenerator.create()
 *       .withSlug("deep-dive")
 *       .withTitle("Playwright Deep Dive")
 *       .withFavoritesCount(42);
 *   VisitorCommands.visitArticleDetail(page, mocker, gen);
 * }</pre>
 */
public final class VisitorCommands {

    private VisitorCommands() {}

    // ── Article Feed ──────────────────────────────────────────────────────────

    /**
     * Navigates to the article feed page with 10 auto-generated articles (zero config).
     */
    public static void visitArticleFeed(Page page, RequestMocker mocker) {
        ArticleFeedVisitor.create(page, mocker).visit();
    }

    /**
     * Navigates to the article feed page with a specific list of articles.
     */
    public static void visitArticleFeed(Page page, RequestMocker mocker, List<ArticleModel> articles) {
        ArticleFeedVisitor.create(page, mocker)
                .withArticles(articles)
                .visit();
    }

    /**
     * Navigates to the article feed page in a specific mode (MOCK or LIVE).
     * Useful for tests that need to explicitly control the mock/live toggle without
     * relying on the application.properties value.
     */
    public static void visitArticleFeed(Page page, RequestMocker mocker, MockMode mode) {
        ArticleFeedVisitor.create(page, mocker, mode).visit();
    }

    // ── Article Detail ────────────────────────────────────────────────────────

    /**
     * Navigates to the article detail page for a given slug.
     * An auto-generated article with that slug is mocked.
     */
    public static void visitArticleDetail(Page page, RequestMocker mocker, String slug) {
        ArticleDetailVisitor.create(page, mocker)
                .withArticle(ArticleGenerator.create().withSlug(slug))
                .visit();
    }

    /**
     * Navigates to the article detail page using a fully configured generator.
     * The generator's slug drives both the mock URL and the navigation URL.
     */
    public static void visitArticleDetail(Page page, RequestMocker mocker, ArticleGenerator generator) {
        ArticleDetailVisitor.create(page, mocker)
                .withArticle(generator)
                .visit();
    }

    /**
     * Navigates to the article detail page with an explicit mode override.
     */
    public static void visitArticleDetail(Page page, RequestMocker mocker, MockMode mode,
                                          ArticleGenerator generator) {
        ArticleDetailVisitor.create(page, mocker, mode)
                .withArticle(generator)
                .visit();
    }
}
