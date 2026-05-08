package com.example.ui.steps;

import com.example.ui.infrastructure.UiTestExtension;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;

/**
 * Cucumber lifecycle hooks for all {@code @ui} scenarios.
 * <p>
 * {@code @Before} creates a fresh Playwright {@link com.microsoft.playwright.BrowserContext}
 * and {@link com.microsoft.playwright.Page} for each scenario.
 * {@code @After} captures a screenshot on failure and closes the context.
 * </p>
 */
@Slf4j
public class UiHooks {

    @Before("@ui")
    public void beforeScenario(Scenario scenario) {
        log.info("[HOOK] Starting: {}", scenario.getName());
        UiTestExtension.beforeScenario();
    }

    @After("@ui")
    public void afterScenario(Scenario scenario) {
        UiTestExtension.afterScenario(scenario.isFailed(), scenario.getName());
    }
}
