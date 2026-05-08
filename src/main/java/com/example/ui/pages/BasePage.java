package com.example.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Base class for all Page Objects.
 * <p>
 * Enforces the rule that all locators MUST use {@code data-testid} attributes.
 * Never use CSS selectors, XPath, or text matchers directly in Page Objects —
 * always add a {@code data-testid} to the frontend component first.
 * </p>
 */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    /**
     * Returns a locator for any element with the given {@code data-testid} attribute.
     * This is the only locator strategy used in this framework.
     */
    protected Locator byTestId(String testId) {
        return page.locator("[data-testid='" + testId + "']");
    }

    /**
     * Returns true if an element with the given {@code data-testid} is currently visible.
     */
    public boolean isVisible(String testId) {
        return byTestId(testId).isVisible();
    }

    /**
     * Returns the trimmed text content of the element with the given {@code data-testid}.
     */
    public String getTextOf(String testId) {
        return byTestId(testId).textContent().trim();
    }
}
