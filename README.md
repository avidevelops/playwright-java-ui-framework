# Playwright + Cucumber UI Automation Framework (Java)

A sample Playwright + Cucumber BDD UI automation framework in Java demonstrating:

- **Mock mode**: Playwright network interception — zero backend dependency
- **Live mode**: real HTTP requests against a running backend
- **Factory / Builder pattern** for runtime mock data generation — no static JSON fixtures
- **Visitor pattern** for page navigation + mock registration in one step
- **Page Object Model** with `data-testid` locators only
- **Scenario-scoped DI** via PicoContainer — clean state between scenarios

## Target application

[Conduit](https://demo.realworld.how) — an open-source Medium.com clone built with React.  
API spec: https://realworld-docs.netlify.app/docs/specs/backend-specs/endpoints

---

## Project structure

```
src/
├── main/java/com/example/ui/
│   ├── infrastructure/          # Playwright lifecycle, mock mode, constants
│   │   ├── PlaywrightProvider   # Browser singleton
│   │   ├── UiTestExtension      # Per-scenario Page/Context/Mocker via ThreadLocal
│   │   ├── RequestMocker        # Fluent page.route() builder
│   │   ├── MockMode             # MOCK / LIVE enum driven by application.properties
│   │   ├── UiConstants          # Base URL, headless, viewport (from properties)
│   │   └── PropertyLoader       # Reads application.properties
│   ├── backend/articles/
│   │   ├── models/              # Java records matching API response shapes exactly
│   │   ├── generators/          # Fluent factory builders with realistic defaults
│   │   └── ArticlesMockApi      # One method per endpoint; calls RequestMocker
│   ├── pages/                   # Page Objects — BasePage + one class per page
│   └── visitors/                # One Visitor per page: registers mocks + navigates
└── test/java/com/example/ui/
    ├── runner/UiTestRunner       # JUnit Platform Suite entry point
    └── steps/
        ├── UiHooks               # @Before/@After lifecycle
        ├── ScenarioContext       # PicoContainer-managed shared state
        ├── MockSetupSteps        # @Given steps: configure generators before navigation
        ├── NavigationSteps       # @Given/@When steps: create Visitor, call .visit()
        └── ArticleSteps          # @Then/@When steps: assertions and interactions
```

---

## How to run

**Prerequisites**: Java 21+, Maven 3.9+. Playwright downloads Chromium automatically on first run.

```bash
# Mock mode — no frontend or backend needed
mvn test -P ui-mock

# Live mode — structural tests only (requires frontend running at qa.ui.baseUrl)
mvn test -P ui-live

# Run with visible browser for local debugging
mvn test -P ui-mock -Dqa.ui.headless=false
```

---

## Tag conventions

| Tag | Applied to | Meaning |
|---|---|---|
| `@ui` | Feature | Marks a feature as a UI test — required on every feature |
| `@live` | Scenario | Structural assertion (element visible, URL change) — safe in both modes |
| *(no extra tag)* | Scenario | Data-sensitive assertion (exact counts, titles) — mock mode only |

**No `@mock` tag exists.** The `qa.ui.mode=mock` property in `application.properties`
controls whether Playwright intercepts API calls — there is no need to tag individual
scenarios for mock vs live. The only meaningful signal at the scenario level is `@live`:
*"I, the author, guarantee this scenario is correct regardless of what the real backend returns."*

---

## How to add tests for a new page

### 1 — Add models (if the page calls a new endpoint)

```java
// src/main/java/com/example/ui/backend/articles/models/
public record NewResponseModel(String id, String name, int count) {}
```
Match field names to the backend response **exactly**, including any intentional typos in the API contract.

### 2 — Add a generator

```java
public class NewResponseGenerator {
    private String id;
    private String name;
    private int count;

    public NewResponseGenerator() {
        int seq = COUNTER.getAndIncrement();
        this.id = "item-" + seq;
        this.name = "Sample Item " + seq;
        this.count = seq * 3;
    }

    public static NewResponseGenerator create() { return new NewResponseGenerator(); }

    public NewResponseGenerator withName(String name) { this.name = name; return this; }
    public NewResponseGenerator withCount(int count)  { this.count = count; return this; }

    public NewResponseModel build() {
        return new NewResponseModel(id, name, count);
    }
}
```

### 3 — Add a mock API method

```java
// In ArticlesMockApi (or create NewPageMockApi)
public String mockGetNewData(NewResponseModel response) {
    return mocker
            .to("/api/new-endpoint")
            .withStatus(200)
            .as("getNewData")
            .get(response);
}
```

### 4 — Create a Visitor

```java
public class NewPageVisitor implements PageVisitor {
    private final Page page;
    private final RequestMocker mocker;
    private NewResponseGenerator generator = NewResponseGenerator.create(); // safe default

    public static NewPageVisitor create(Page page, RequestMocker mocker) {
        return new NewPageVisitor(page, mocker);
    }

    public NewPageVisitor withData(NewResponseGenerator gen) {
        this.generator = gen;
        return this;
    }

    @Override
    public void visit() {
        if (UiTestExtension.getMode().isMocked()) {
            new ArticlesMockApi(mocker).mockGetNewData(generator.build());
        }
        page.navigate(UiConstants.BASE_URL + NewPage.PATH);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
```

### 5 — Create a Page Object

```java
public class NewPage extends BasePage {
    public static final String PATH = "/new-page";

    public NewPage(Page page) { super(page); }

    // Use data-testid attributes exclusively — no CSS selectors or XPath
    public boolean isListVisible()      { return isVisible("item-list"); }
    public int     getItemCount()       { return byTestId("list-item").count(); }
    public String  getFirstItemName()   { return getTextOf("item-name"); }
}
```

### 6 — Add step definitions

In `MockSetupSteps`:
```java
@Given("the page shows {int} items")
public void pageShowsItems(int count) {
    context.setNewDataGenerator(NewResponseGenerator.create().withCount(count));
}
```

In `NavigationSteps`:
```java
@Given("the user is on the new page")
public void theUserIsOnNewPage() {
    Page page = UiTestExtension.getPage();
    RequestMocker mocker = UiTestExtension.getMocker();
    NewPageVisitor visitor = NewPageVisitor.create(page, mocker);
    if (context.getNewDataGenerator() != null) {
        visitor.withData(context.getNewDataGenerator());
    }
    visitor.visit();
}
```

### 7 — Write the feature file

```gherkin
@ui
Feature: New Page
  As a user
  I want to see my items on the new page
  So that I can manage them

  @live
  Scenario: New page loads successfully
    Given the user is on the new page
    Then the item list is visible

  Scenario: New page shows configured number of items
    Given the page shows 5 items
    And the user is on the new page
    Then the item count is 5
```

---

## Architecture: why Visitor + Generator?

The core design insight is separation of **what data to show** from **how to navigate**:

```
Test author (Gherkin)
    │ configures generators in ScenarioContext via @Given steps
    ▼
NavigationSteps
    │ creates Visitor, passes generators, calls .visit()
    ▼
Visitor
    │ mock mode → calls *MockApi → RequestMocker registers page.route() intercepts
    │ live mode → skips mock registration entirely
    │ navigates to page URL, waits for network idle
    ▼
Page (browser renders, API calls are intercepted or hit real backend)
    │
    ▼
AssertionSteps
    │ instantiates Page Object, asserts via data-testid locators
    ▼
Test passes/fails
```

This means:
- **Zero-config scenarios** work immediately — generators have realistic defaults
- **Single-field overrides** require one `@Given` step; everything else stays auto-generated
- **Mock and live modes share the same scenarios** — the Visitor simply skips `registerMocks()` in live mode
- **No static JSON fixtures** — all mock data is generated at runtime and can be inspected in assertions

---

## data-testid requirements

All Page Object locators use `data-testid` attributes. If running against the real Conduit frontend,
add these attributes to the React components:

| `data-testid` value | Element | Component |
|---|---|---|
| `article-feed-item` | article card `<div>` | `ArticlePreview.tsx` |
| `article-title` | title `<h1>` or `<a>` | `ArticlePreview.tsx`, `ArticlePage.tsx` |
| `article-author` | author name `<a>` | `ArticleMeta.tsx` |
| `article-date` | date `<span>` | `ArticleMeta.tsx` |
| `article-body` | body `<div>` | `ArticlePage.tsx` |
| `article-favorites-count` | favorites count `<span>` | `ArticleMeta.tsx` |
| `article-favorite-button` | favorite button | `ArticleMeta.tsx` |
| `tab-global-feed` | global feed tab | `ArticleList.tsx` |
| `pagination-next` | next page button | `ListPagination.tsx` |
| `pagination-previous` | previous page button | `ListPagination.tsx` |
| `pagination-current` | active page button | `ListPagination.tsx` |
| `back-to-feed` | back link | `ArticlePage.tsx` |
