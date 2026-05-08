package com.example.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object for the Article Feed page (route: {@code /}).
 *
 * <h3>Required data-testid attributes in the frontend:</h3>
 * <ul>
 *   <li>{@code article-feed-item}  — each article card container</li>
 *   <li>{@code article-title}      — article title inside each card</li>
 *   <li>{@code article-author}     — author name inside each card</li>
 *   <li>{@code tab-global-feed}    — "Global Feed" tab button</li>
 *   <li>{@code pagination-next}    — next page button</li>
 *   <li>{@code pagination-previous}— previous page button</li>
 *   <li>{@code pagination-current} — active page number button</li>
 * </ul>
 */
public class ArticleFeedPage extends BasePage {

    public static final String PATH = "/";

    public ArticleFeedPage(Page page) {
        super(page);
    }

    // ── Article list ─────────────────────────────────────────────────────────

    public Locator getArticleItems() {
        return byTestId("article-feed-item");
    }

    public int getArticleCount() {
        return getArticleItems().count();
    }

    public String getTitleAtRow(int index) {
        return getArticleItems().nth(index)
                .locator("[data-testid='article-title']")
                .textContent().trim();
    }

    public String getAuthorAtRow(int index) {
        return getArticleItems().nth(index)
                .locator("[data-testid='article-author']")
                .textContent().trim();
    }

    public void clickArticle(int index) {
        getArticleItems().nth(index).click();
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────

    public void clickGlobalFeedTab() {
        byTestId("tab-global-feed").click();
    }

    // ── Pagination ───────────────────────────────────────────────────────────

    public void clickNextPage() {
        byTestId("pagination-next").click();
    }

    public void clickPreviousPage() {
        byTestId("pagination-previous").click();
    }

    public String getCurrentPage() {
        return getTextOf("pagination-current");
    }
}
