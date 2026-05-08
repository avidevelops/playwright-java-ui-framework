package com.example.ui.backend.articles.generators;

import com.example.ui.backend.articles.models.ArticleModel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fluent factory builder for {@link ArticleModel} test data.
 * <p>
 * Every field is initialised to a realistic non-null default in the constructor,
 * so a zero-config {@code ArticleGenerator.create().build()} always produces a
 * valid article — no step setup required for basic scenarios.
 * </p>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 *   // Default article — no setup needed
 *   ArticleModel article = ArticleGenerator.create().build();
 *
 *   // Single-field override — everything else stays auto-generated
 *   ArticleModel article = ArticleGenerator.create()
 *       .withTitle("How to Use Playwright")
 *       .withAuthorUsername("jane-doe")
 *       .build();
 * }</pre>
 */
public class ArticleGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private String slug;
    private String title;
    private String description;
    private String body;
    private List<String> tagList;
    private String createdAt;
    private String updatedAt;
    private boolean favorited;
    private int favoritesCount;
    private String authorUsername;
    private String authorBio;
    private String authorImage;

    public ArticleGenerator() {
        int seq = COUNTER.getAndIncrement();
        this.slug             = "sample-article-" + seq;
        this.title            = "Sample Article " + seq;
        this.description      = "A concise description of sample article " + seq + ".";
        this.body             = "This is the full body content of sample article " + seq + ". "
                                + "It contains multiple sentences to simulate realistic content.";
        this.tagList          = List.of("sample", "automation", "test" + seq);
        this.createdAt        = "2024-" + String.format("%02d", (seq % 12) + 1)
                                + "-" + String.format("%02d", (seq % 28) + 1)
                                + "T10:00:00.000Z";
        this.updatedAt        = this.createdAt;
        this.favorited        = false;
        this.favoritesCount   = seq * 3;
        this.authorUsername   = "author-" + seq;
        this.authorBio        = "Bio for test author " + seq;
        this.authorImage      = "https://api.dicebear.com/7.x/avataaars/svg?seed=author" + seq;
    }

    /** Creates a new generator with default values. Each call produces a unique slug/title. */
    public static ArticleGenerator create() {
        return new ArticleGenerator();
    }

    // ── Fluent setters ──────────────────────────────────────────────────────

    public ArticleGenerator withSlug(String slug)                 { this.slug = slug;                   return this; }
    public ArticleGenerator withTitle(String title)               { this.title = title;                 return this; }
    public ArticleGenerator withDescription(String description)   { this.description = description;     return this; }
    public ArticleGenerator withBody(String body)                 { this.body = body;                   return this; }
    public ArticleGenerator withTags(List<String> tags)           { this.tagList = tags;                return this; }
    public ArticleGenerator withCreatedAt(String createdAt)       { this.createdAt = createdAt;         return this; }
    public ArticleGenerator withFavorited(boolean favorited)      { this.favorited = favorited;         return this; }
    public ArticleGenerator withFavoritesCount(int count)         { this.favoritesCount = count;        return this; }
    public ArticleGenerator withAuthorUsername(String username)   { this.authorUsername = username;     return this; }
    public ArticleGenerator withAuthorBio(String bio)             { this.authorBio = bio;               return this; }

    /** Getter used by NavigationSteps to determine the URL path for navigation. */
    public String getSlug() { return slug; }

    // ── Build ────────────────────────────────────────────────────────────────

    public ArticleModel build() {
        return new ArticleModel(
                slug, title, description, body, tagList,
                createdAt, updatedAt, favorited, favoritesCount,
                new ArticleModel.Author(authorUsername, authorBio, authorImage, false)
        );
    }
}
