package com.example.ui.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * JUnit Platform Suite entry point for all UI tests.
 *
 * <h3>Running via Maven profiles:</h3>
 * <pre>
 *   # Mock mode — all @ui scenarios, no backend needed
 *   mvn test -P ui-mock
 *
 *   # Live mode — @live scenarios only, requires running frontend + backend
 *   mvn test -P ui-live
 *
 *   # With visible browser (local debugging)
 *   mvn test -P ui-mock -Dqa.ui.headless=false
 * </pre>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,   value = "com.example.ui.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, "
        + "html:target/cucumber-reports/report.html, "
        + "json:target/cucumber-reports/report.json")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@ui")
public class UiTestRunner {}
