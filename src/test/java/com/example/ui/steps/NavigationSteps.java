package com.example.ui.steps;

import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.infrastructure.RequestMocker;
import com.example.ui.infrastructure.UiConstants;
import com.example.ui.infrastructure.UiTestExtension;
import com.example.ui.pages.ArticleFeedPage;
import com.example.ui.visitors.ArticleDetailVisitor;
import com.example.ui.visitors.ArticleFeedVisitor;
import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

/**
 * Navigation step definitions for UI tests.
 * <p>
 * Each step:
 * <ol>
 *   <li>Creates the appropriate Visitor</li>
 *   <li>Checks {@link ScenarioContext} for any overrides and passes them to the Visitor</li>
 *   <li>Calls {@code visitor.visit()} — which registers mocks (if in mock mode) and navigates</li>
 * </ol>
 * </p>
 *
 * <h3>Null-safety rule:</h3>
 * Always null-check context fields before passing to {@code with*()} methods.
 * Visitors initialise their own safe defaults in the constructor, so it is always
 * safe to skip the override — the Visitor will still work correctly.
 */
@Slf4j
public class NavigationSteps {

    private final ScenarioContext context;

    public NavigationSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("the user is on the article feed page")
    public void theUserIsOnArticleFeedPage() {
        Page page = UiTestExtension.getPage();
        RequestMocker mocker = UiTestExtension.getMocker();

        ArticleFeedVisitor visitor = ArticleFeedVisitor.create(page, mocker);

        // Only override if MockSetupSteps configured specific articles.
        // Otherwise the Visitor's default (10 auto-generated articles) is used.
        if (!context.getArticles().isEmpty()) {
            visitor.withArticles(context.getArticles());
        }

        visitor.visit();
    }

    @Given("the user is on the article detail page for {string}")
    public void theUserIsOnArticleDetailPage(String slug) {
        Page page = UiTestExtension.getPage();
        RequestMocker mocker = UiTestExtension.getMocker();

        ArticleDetailVisitor visitor = ArticleDetailVisitor.create(page, mocker);

        if (context.getArticleGenerator() != null) {
            // Use the generator configured by MockSetupSteps (has specific slug/title/author).
            visitor.withArticle(context.getArticleGenerator());
        } else {
            // No setup step ran — create a minimal generator from the slug in the step.
            visitor.withArticle(ArticleGenerator.create().withSlug(slug));
        }

        visitor.visit();
    }

    @When("the user navigates back to the feed")
    public void theUserNavigatesBackToFeed() {
        Page page = UiTestExtension.getPage();
        page.navigate(UiConstants.BASE_URL + ArticleFeedPage.PATH);
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }
}
