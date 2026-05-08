package com.example.ui.pages;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Article Detail page (route: {@code /article/{slug}}).
 *
 * <h3>Required data-testid attributes in the frontend:</h3>
 * <ul>
 *   <li>{@code article-title}          — article title {@code <h1>}</li>
 *   <li>{@code article-author}         — author name {@code <a>}</li>
 *   <li>{@code article-date}           — publication date {@code <span>}</li>
 *   <li>{@code article-body}           — body content {@code <div>}</li>
 *   <li>{@code article-tag-item}       — each tag {@code <li>} in the tag list</li>
 *   <li>{@code article-favorites-count}— favorites count {@code <span>}</li>
 *   <li>{@code article-favorite-button}— favorite button</li>
 *   <li>{@code back-to-feed}           — "← Back to feed" link</li>
 * </ul>
 */
public class ArticleDetailPage extends BasePage {

    public static final String PATH_TEMPLATE = "/article/%s";

    public ArticleDetailPage(Page page) {
        super(page);
    }

    public static String pathFor(String slug) {
        return String.format(PATH_TEMPLATE, slug);
    }

    // ── Header ───────────────────────────────────────────────────────────────

    public String getTitle() {
        return getTextOf("article-title");
    }

    public String getAuthor() {
        return getTextOf("article-author");
    }

    public String getDate() {
        return getTextOf("article-date");
    }

    // ── Body ─────────────────────────────────────────────────────────────────

    public String getBody() {
        return getTextOf("article-body");
    }

    // ── Meta ─────────────────────────────────────────────────────────────────

    public String getFavoritesCount() {
        return getTextOf("article-favorites-count");
    }

    public boolean isFavoriteButtonVisible() {
        return isVisible("article-favorite-button");
    }

    public int getTagCount() {
        return byTestId("article-tag-item").count();
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    public void clickBackToFeed() {
        byTestId("back-to-feed").click();
    }
}
