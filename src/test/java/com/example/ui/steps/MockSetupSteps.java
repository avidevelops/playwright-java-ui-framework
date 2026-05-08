package com.example.ui.steps;

import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.backend.articles.models.ArticleModel;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Step definitions for configuring mock data before page navigation.
 * <p>
 * Each step mutates generators or lists stored in {@link ScenarioContext}.
 * The {@link NavigationSteps} then reads these when creating Visitors — so
 * mock data setup and navigation are cleanly separated.
 * </p>
 *
 * <h3>Design rule — null safety:</h3>
 * Visitors always check context fields for null before applying them, so it is
 * safe to skip any of these steps. The Visitor's own defaults will be used.
 *
 * <h3>Example Gherkin:</h3>
 * <pre>
 *   Given the feed has 5 articles
 *   Given an article with slug "how-to-test" and title "How to Test" exists
 *   Given the article is written by "jane-doe"
 * </pre>
 */
@Slf4j
public class MockSetupSteps {

    private final ScenarioContext context;

    public MockSetupSteps(ScenarioContext context) {
        this.context = context;
    }

    // ── Feed page setup ───────────────────────────────────────────────────────

    /**
     * Creates N auto-generated articles and puts them in the feed.
     * Each article gets a unique sequential slug and title.
     */
    @Given("the feed has {int} articles")
    public void theFeedHasArticles(int count) {
        List<ArticleModel> articles = IntStream.range(0, count)
                .mapToObj(i -> ArticleGenerator.create().build())
                .toList();
        context.setArticles(articles);
        log.debug("[MOCK SETUP] {} articles configured for feed", count);
    }

    /**
     * Creates a single article with specific title and adds it to the feed.
     * Can be called multiple times to build up a list of articles with known titles.
     */
    @Given("the feed contains an article with title {string}")
    public void theFeedContainsArticleWithTitle(String title) {
        ArticleModel article = ArticleGenerator.create().withTitle(title).build();
        context.addArticle(article);
        log.debug("[MOCK SETUP] Added article to feed: '{}'", title);
    }

    // ── Detail page setup ─────────────────────────────────────────────────────

    /**
     * Creates an article generator with the given slug and title.
     * Used by NavigationSteps to mock GET /api/articles/{slug} and navigate to /article/{slug}.
     */
    @Given("an article with slug {string} and title {string} exists")
    public void anArticleWithSlugAndTitleExists(String slug, String title) {
        context.setArticleGenerator(
                ArticleGenerator.create().withSlug(slug).withTitle(title)
        );
        log.debug("[MOCK SETUP] Article configured: slug='{}', title='{}'", slug, title);
    }

    /**
     * Sets the author on the article generator already configured in context.
     * Call after {@code an article with slug ... and title ... exists}.
     */
    @Given("the article is written by {string}")
    public void theArticleIsWrittenBy(String authorUsername) {
        if (context.getArticleGenerator() == null) {
            context.setArticleGenerator(ArticleGenerator.create());
        }
        context.getArticleGenerator().withAuthorUsername(authorUsername);
        log.debug("[MOCK SETUP] Article author set to '{}'", authorUsername);
    }

    /**
     * Sets the favorites count on the article generator already configured in context.
     * Call after {@code an article with slug ... and title ... exists}.
     */
    @Given("the article has {int} favorites")
    public void theArticleHasFavorites(int count) {
        if (context.getArticleGenerator() == null) {
            context.setArticleGenerator(ArticleGenerator.create());
        }
        context.getArticleGenerator().withFavoritesCount(count);
        log.debug("[MOCK SETUP] Article favorites set to {}", count);
    }

    /**
     * Sets the tags on the article generator already configured in context.
     * Tags are provided as a comma-separated string (e.g. {@code "java, testing, playwright"}).
     */
    @Given("the article has tags {string}")
    public void theArticleHasTags(String commaSeparatedTags) {
        if (context.getArticleGenerator() == null) {
            context.setArticleGenerator(ArticleGenerator.create());
        }
        List<String> tags = List.of(commaSeparatedTags.split(",\\s*"));
        context.getArticleGenerator().withTags(tags);
        log.debug("[MOCK SETUP] Article tags set to {}", tags);
    }
}
