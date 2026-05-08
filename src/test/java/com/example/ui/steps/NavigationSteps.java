package com.example.ui.steps;

import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.infrastructure.RequestMocker;
import com.example.ui.infrastructure.UiConstants;
import com.example.ui.infrastructure.UiTestExtension;
import com.example.ui.pages.ArticleFeedPage;
import com.example.ui.support.VisitorCommands;
import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

/**
 * Navigation step definitions for UI tests.
 * <p>
 * Each step delegates to {@link VisitorCommands} — the single authoritative entry-point
 * for page navigation. Step definitions never instantiate Visitor classes directly;
 * they only read from {@link ScenarioContext} to decide which overload of
 * {@code VisitorCommands.visit*()} to call.
 * </p>
 *
 * <h3>Why delegate to VisitorCommands?</h3>
 * <ul>
 *   <li>Keeps the {@link VisitorCommands} facade as the single source of truth for
 *       default data generation logic — no duplication between Cucumber and JUnit 5 paths.</li>
 *   <li>Ensures {@link com.example.ui.infrastructure.AppInitMocker} is always wired in,
 *       since {@code VisitorCommands} calls visitors that include it.</li>
 *   <li>Makes step definitions trivially small — they only resolve context and delegate.</li>
 * </ul>
 *
 * <h3>Null-safety rule:</h3>
 * Always null-check context fields before branching. Passing {@code null} to
 * {@code VisitorCommands} is never safe — use the no-arg overload as the fallback.
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

        if (!context.getArticles().isEmpty()) {
            // Scenario configured specific articles via MockSetupSteps — use them.
            log.debug("[NAV] Visiting feed with {} configured articles", context.getArticles().size());
            VisitorCommands.visitArticleFeed(page, mocker, context.getArticles());
        } else {
            // No setup step ran — use zero-config default (10 auto-generated articles).
            log.debug("[NAV] Visiting feed with zero-config defaults");
            VisitorCommands.visitArticleFeed(page, mocker);
        }
    }

    @Given("the user is on the article detail page for {string}")
    public void theUserIsOnArticleDetailPage(String slug) {
        Page page = UiTestExtension.getPage();
        RequestMocker mocker = UiTestExtension.getMocker();

        if (context.getArticleGenerator() != null) {
            // MockSetupSteps configured a specific generator (has slug/title/author set).
            log.debug("[NAV] Visiting detail with configured generator (slug={})",
                    context.getArticleGenerator().getSlug());
            VisitorCommands.visitArticleDetail(page, mocker, context.getArticleGenerator());
        } else {
            // No setup step ran — create a minimal generator from the Gherkin step slug.
            log.debug("[NAV] Visiting detail with step slug '{}'", slug);
            VisitorCommands.visitArticleDetail(page, mocker,
                    ArticleGenerator.create().withSlug(slug));
        }
    }

    @When("the user navigates back to the feed")
    public void theUserNavigatesBackToFeed() {
        Page page = UiTestExtension.getPage();
        page.navigate(UiConstants.BASE_URL + ArticleFeedPage.PATH);
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        log.debug("[NAV] Navigated back to feed");
    }
}
