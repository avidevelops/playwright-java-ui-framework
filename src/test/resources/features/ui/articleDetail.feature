@ui
Feature: Article Detail Page
  As a reader of the Conduit blog
  I want to view a specific article's full content and metadata
  So that I can read and interact with it

  # ───────────────────────────────────────────────────────────────────────────
  # STRUCTURAL: page renders — works in BOTH mock and live.
  # These scenarios navigate to a known slug and assert only that the page
  # loaded, not the specific content returned by the API.
  # ───────────────────────────────────────────────────────────────────────────

  @live
  Scenario: Article detail page loads for a given slug
    Given the user is on the article detail page for "how-to-use-playwright"
    Then the page URL contains "how-to-use-playwright"

  @live
  Scenario: Article detail page renders a title and a favorite button
    Given the user is on the article detail page for "how-to-use-playwright"
    Then the favorite button is visible

  # ───────────────────────────────────────────────────────────────────────────
  # DATA-SENSITIVE: assertions depend on injected mock data.
  # Skipped in live mode — real API content changes between runs.
  #
  # PATTERN 1 — Zero config: Visitor generates a default article automatically.
  # ───────────────────────────────────────────────────────────────────────────

  Scenario: Article detail page loads with default generated data
    Given the user is on the article detail page for "how-to-use-playwright"
    Then the article detail page shows 0 favorites

  # ───────────────────────────────────────────────────────────────────────────
  # PATTERN 2 — Single override: control one specific field.
  # ───────────────────────────────────────────────────────────────────────────

  Scenario: Article detail page shows the injected title
    Given an article with slug "playwright-deep-dive" and title "Playwright Deep Dive" exists
    And the user is on the article detail page for "playwright-deep-dive"
    Then the article detail page shows title "Playwright Deep Dive"

  Scenario: Article detail page shows the correct author
    Given an article with slug "author-showcase" and title "Author Showcase" exists
    And the article is written by "jane-doe"
    And the user is on the article detail page for "author-showcase"
    Then the article detail page shows author "jane-doe"

  # ───────────────────────────────────────────────────────────────────────────
  # PATTERN 3 — Multiple overrides: control several fields together.
  # ───────────────────────────────────────────────────────────────────────────

  Scenario: Article detail page shows the configured favorites count
    Given an article with slug "popular-article" and title "Very Popular Article" exists
    And the article has 42 favorites
    And the user is on the article detail page for "popular-article"
    Then the article detail page shows 42 favorites

  Scenario: Article detail page shows the correct tags
    Given an article with slug "tagged-article" and title "A Well-Tagged Article" exists
    And the article has tags "java, playwright, automation"
    And the user is on the article detail page for "tagged-article"
    Then the article detail page has 3 tags
