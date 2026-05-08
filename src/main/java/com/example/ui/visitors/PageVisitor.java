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
    void visit();
}
