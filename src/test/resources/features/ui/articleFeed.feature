@ui
Feature: Article Feed Page
  As a reader of the Conduit blog
  I want to see a list of articles on the feed page
  So that I can discover and navigate to content

  # ───────────────────────────────────────────────────────────────────────────
  # STRUCTURAL: page renders — works in BOTH mock and live.
  # These scenarios assert only that key elements exist and navigation works.
  # They make no assumptions about the specific articles returned by the API.
  # ───────────────────────────────────────────────────────────────────────────

  @live
  Scenario: Article feed loads with at least one article
    Given the user is on the article feed page
    Then the article feed is visible

  @live
  Scenario: Clicking an article navigates to the detail page
    Given the user is on the article feed page
    When the user clicks on article row 1
    Then the page URL contains "article"

  # ───────────────────────────────────────────────────────────────────────────
  # DATA-SENSITIVE: assertions depend on injected mock data.
  # Skipped in live mode — the real API will return different articles each run.
  #
  # PATTERN 1 — Zero config: no Given setup steps needed.
  # The Visitor initialises 10 auto-generated articles by default.
  # ───────────────────────────────────────────────────────────────────────────

  Scenario: Article feed shows 10 articles by default
    Given the user is on the article feed page
    Then the article feed displays 10 articles

  # ───────────────────────────────────────────────────────────────────────────
  # PATTERN 2 — Single override: only the field under test is configured.
  # Everything else stays auto-generated.
  # ───────────────────────────────────────────────────────────────────────────

  Scenario: Article feed displays the configured number of articles
    Given the feed has 5 articles
    And the user is on the article feed page
    Then the article feed displays 5 articles

  Scenario: Article feed displays 1 article when only one is available
    Given the feed has 1 articles
    And the user is on the article feed page
    Then the article feed displays 1 articles

  # ───────────────────────────────────────────────────────────────────────────
  # PATTERN 3 — Specific data: test a known title in a known position.
  # ───────────────────────────────────────────────────────────────────────────

  Scenario: Article feed shows the correct title for a specific article
    Given the feed contains an article with title "Introduction to Playwright"
    And the feed contains an article with title "Advanced Mocking Techniques"
    And the user is on the article feed page
    Then article row 1 shows title "Introduction to Playwright"
    And article row 2 shows title "Advanced Mocking Techniques"
