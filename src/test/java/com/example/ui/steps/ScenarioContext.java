package com.example.ui.steps;

import com.example.ui.backend.articles.generators.ArticleGenerator;
import com.example.ui.backend.articles.models.ArticleModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenario-scoped state shared across all step definition classes within one scenario.
 * <p>
 * Cucumber (via PicoContainer) creates one fresh instance per scenario and injects it
 * into every step class constructor. This allows {@code MockSetupSteps} (which configures
 * data) to communicate with {@code NavigationSteps} (which creates Visitors) and
 * {@code ArticleSteps} (which makes assertions) — all without static fields or
 * test-framework-specific scoping annotations.
 * </p>
 *
 * <h3>Pattern:</h3>
 * <ol>
 *   <li>MockSetupSteps fills fields via setters ({@code @Given} steps)</li>
 *   <li>NavigationSteps reads fields to configure Visitors</li>
 *   <li>Visitors null-check each field before applying it — safe defaults remain
 *       if no setup step was called</li>
 * </ol>
 */
public class ScenarioContext {

    // Used by ArticleDetailVisitor — null means "use generator default"
    private ArticleGenerator articleGenerator;

    // Used by ArticleFeedVisitor — empty list means "use visitor default (10 auto-generated)"
    private List<ArticleModel> articles;

    public ScenarioContext() {
        this.articles = new ArrayList<>();
    }

    // ── Article generator (drives detail page mock + navigation URL) ──────────

    public ArticleGenerator getArticleGenerator()                  { return articleGenerator; }
    public void setArticleGenerator(ArticleGenerator generator)    { this.articleGenerator = generator; }

    // ── Article list (drives feed page mock) ─────────────────────────────────

    public List<ArticleModel> getArticles()                        { return articles; }
    public void setArticles(List<ArticleModel> articles)           { this.articles = articles; }
    public void addArticle(ArticleModel article)                   { this.articles.add(article); }
}
