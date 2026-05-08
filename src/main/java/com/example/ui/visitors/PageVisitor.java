package com.example.ui.visitors;

/**
 * Contract for all page visitors.
 * <p>
 * A Visitor is responsible for two things:
 * <ol>
 *   <li>Registering Playwright route intercepts (mock mode only)</li>
 *   <li>Navigating to the page URL and waiting for network idle</li>
 * </ol>
 * Both are encapsulated in a single {@link #visit()} call, so NavigationSteps
 * only needs to create the visitor, set any overrides, and call {@code .visit()}.
 * </p>
 */
public interface PageVisitor {
    /**
     * Registers all required mocks (if in MOCK mode), then navigates to {@link #pageUrl()}.
     * This is the single entry point tests use for page navigation.
     */
    void visit();

    /**
     * Returns the fully-qualified URL for this page (e.g., {@code "http://localhost:4100/"}).
     * Useful for logging, assertions on {@code page.url()}, and {@link com.example.ui.support.VisitorCommands}.
     */
    String pageUrl();
}
