package com.example.ui.steps;

import com.example.ui.infrastructure.UiTestExtension;
import com.example.ui.pages.ArticleDetailPage;
import com.example.ui.pages.ArticleFeedPage;
import com.microsoft.playwright.Page;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertion and interaction step definitions for article-related pages.
 */
@Slf4j
public class ArticleSteps {

    private final ScenarioContext context;

    public ArticleSteps(ScenarioContext context) {
        this.context = context;
    }

    // ── Article feed assertions ───────────────────────────────────────────────

    @Then("the article feed is visible")
    public void articleFeedIsVisible() {
        Page page = UiTestExtension.getPage();
        assertThat(new ArticleFeedPage(page).getArticleCount())
                .as("Expected at least one article in the feed")
                .isGreaterThan(0);
    }

    @Then("the article feed displays {int} articles")
    public void articleFeedDisplays(int expectedCount) {
        Page page = UiTestExtension.getPage();
        assertThat(new ArticleFeedPage(page).getArticleCount())
                .as("Article count in feed")
                .isEqualTo(expectedCount);
    }

    @Then("article row {int} shows title {string}")
    public void articleRowShowsTitle(int row, String expectedTitle) {
        Page page = UiTestExtension.getPage();
        assertThat(new ArticleFeedPage(page).getTitleAtRow(row - 1))
                .as("Title at row %d", row)
                .isEqualTo(expectedTitle);
    }

    @Then("article row {int} shows author {string}")
    public void articleRowShowsAuthor(int row, String expectedAuthor) {
        Page page = UiTestExtension.getPage();
        assertThat(new ArticleFeedPage(page).getAuthorAtRow(row - 1))
                .as("Author at row %d", row)
                .isEqualTo(expectedAuthor);
    }

    // ── Article feed interactions ─────────────────────────────────────────────

    @When("the user clicks on article row {int}")
    public void userClicksArticleRow(int row) {
        Page page = UiTestExtension.getPage();
        new ArticleFeedPage(page).clickArticle(row - 1);
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    // ── Article detail assertions ─────────────────────────────────────────────

    @Then("the article detail page shows title {string}")
    public void articleDetailShowsTitle(String expected) {
        Page page = UiTestExtension.getPage();
        assertThat(new ArticleDetailPage(page).getTitle())
                .as("Article title on detail page")
                .isEqualTo(expected);
    }

    @Then("the article detail page shows author {string}")
    public void articleDetailShowsAuthor(String expected) {
        Page page = UiTestExtension.getPage();
        assertThat(new ArticleDetailPage(page).getAuthor())
                .as("Article author on detail page")
                .isEqualTo(expected);
    }

    @Then("the article detail page shows {int} favorites")
    public void articleDetailShowsFavorites(int expected) {
        Page page = UiTestExtension.getPage();
        assertThat(new ArticleDetailPage(page).getFavoritesCount())
                .as("Favorites count on detail page")
                .contains(String.valueOf(expected));
    }

    @Then("the article detail page has {int} tags")
    public void articleDetailHasTags(int expectedCount) {
        Page page = UiTestExtension.getPage();
        assertThat(new ArticleDetailPage(page).getTagCount())
                .as("Tag count on detail page")
                .isEqualTo(expectedCount);
    }

    @Then("the favorite button is visible")
    public void favoriteButtonIsVisible() {
        Page page = UiTestExtension.getPage();
        assertThat(new ArticleDetailPage(page).isFavoriteButtonVisible())
                .as("Favorite button should be visible")
                .isTrue();
    }

    // ── Common assertions ─────────────────────────────────────────────────────

    @Then("the page URL contains {string}")
    public void pageUrlContains(String fragment) {
        Page page = UiTestExtension.getPage();
        assertThat(page.url())
                .as("Current page URL")
                .contains(fragment);
    }
}
