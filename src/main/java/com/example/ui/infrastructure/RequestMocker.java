package com.example.ui.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fluent builder for registering Playwright {@code page.route()} intercepts.
 * <p>
 * One instance is created per scenario (via {@link UiTestExtension}) and shared
 * across all {@code *MockApi} classes that need to register mocks for that scenario.
 * </p>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 *   mocker.to("/api/articles")
 *         .withStatus(200)
 *         .as("getArticles")
 *         .get(articleListResponse);
 * }</pre>
 *
 * <h3>URL matching:</h3>
 * The pattern is built as {@code "**" + urlPath + "**"} so that:
 * <ul>
 *   <li>Any scheme/host prefix is matched (e.g. {@code https://api.example.com})</li>
 *   <li>Dynamic suffix segments are matched (e.g. {@code /api/articles/some-slug?page=1})</li>
 * </ul>
 */
@Slf4j
public class RequestMocker {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Page page;

    // Stores every intercepted request by alias — keyed list so multiple hits to the same
    // endpoint within one scenario are all retained (e.g. pagination, retries).
    private final ConcurrentHashMap<String, List<InterceptedRequest>> intercepted = new ConcurrentHashMap<>();

    // ── Builder state (reset after each terminal call) ──────────────────────
    private String urlPath;
    private int    statusCode = 200;
    private int    delayMs    = 0;
    private String alias;
    private final Map<String, String> extraHeaders = new HashMap<>();

    public RequestMocker(Page page) {
        this.page = page;
    }

    // ── Fluent builder ──────────────────────────────────────────────────────

    /** URL path to intercept (e.g. {@code "/api/articles"}). */
    public RequestMocker to(String urlPath) {
        this.urlPath = urlPath;
        return this;
    }

    public RequestMocker withStatus(int status) {
        this.statusCode = status;
        return this;
    }

    /** Artificial response delay in milliseconds (0 = no delay). */
    public RequestMocker withDelay(int ms) {
        this.delayMs = ms;
        return this;
    }

    /** Alias used to retrieve the intercepted request later via {@link #getIntercepted(String)}. */
    public RequestMocker as(String alias) {
        this.alias = alias;
        return this;
    }

    public RequestMocker withHeader(String name, String value) {
        this.extraHeaders.put(name, value);
        return this;
    }

    // ── Terminal methods ────────────────────────────────────────────────────

    public String get(Object responseBody)    { return register("GET",    responseBody); }
    public String post(Object responseBody)   { return register("POST",   responseBody); }
    public String put(Object responseBody)    { return register("PUT",    responseBody); }
    public String delete()                    { return register("DELETE", null);         }

    // ── Inspection ──────────────────────────────────────────────────────────

    /**
     * Returns all captured requests for an alias (empty list if none yet).
     * Useful for asserting on pagination calls or retries.
     */
    public List<InterceptedRequest> getIntercepted(String alias) {
        return intercepted.getOrDefault(alias, List.of());
    }

    /**
     * Returns the most recent intercepted request for the alias, or {@code null} if none.
     * Equivalent to Cypress's {@code cy.wait('@alias')} for single-shot assertions.
     */
    public InterceptedRequest getLastIntercepted(String alias) {
        List<InterceptedRequest> hits = intercepted.get(alias);
        if (hits == null || hits.isEmpty()) return null;
        return hits.get(hits.size() - 1);
    }

    /** Returns true if at least one request matching {@code alias} was intercepted. */
    public boolean wasIntercepted(String alias) {
        List<InterceptedRequest> hits = intercepted.get(alias);
        return hits != null && !hits.isEmpty();
    }

    /**
     * Blocks until at least one request for {@code alias} has been intercepted, or throws
     * {@link AssertionError} if the timeout expires.
     * <p>
     * Java equivalent of the Cypress subscriber pattern:
     * {@code var wait = subscribeToX(); triggerAction(); wait();}
     * — here you call {@code waitForAlias()} AFTER the action.
     * </p>
     *
     * @param alias     the alias registered in the mock chain
     * @param timeoutMs maximum time to wait in milliseconds
     * @return the most recent {@link InterceptedRequest} for this alias
     */
    public InterceptedRequest waitForAlias(String alias, int timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            List<InterceptedRequest> hits = intercepted.getOrDefault(alias, List.of());
            if (!hits.isEmpty()) {
                return hits.get(hits.size() - 1);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new AssertionError(
                "No request intercepted for alias '" + alias + "' within " + timeoutMs + "ms");
    }

    // ── Internal ────────────────────────────────────────────────────────────

    private String register(String method, Object responseBody) {
        String pattern           = buildPattern();
        String registeredAlias   = alias != null ? alias : urlPath;
        int    capturedStatus    = statusCode;
        int    capturedDelay     = delayMs;
        Map<String, String> hdrs = new HashMap<>(extraHeaders);
        hdrs.putIfAbsent("Content-Type", "application/json");

        page.route(pattern, route -> {
            if (!route.request().method().equalsIgnoreCase(method)) {
                route.fallback();
                return;
            }
            if (capturedDelay > 0) {
                try { Thread.sleep(capturedDelay); } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            String body = toJson(responseBody);
            route.fulfill(new Route.FulfillOptions()
                    .setStatus(capturedStatus)
                    .setHeaders(hdrs)
                    .setBody(body));
            intercepted.computeIfAbsent(registeredAlias, k -> new CopyOnWriteArrayList<>())
                    .add(new InterceptedRequest(
                            route.request().url(),
                            route.request().method(),
                            safePostBody(route.request()),
                            body
                    ));
            log.debug("[MOCK] {} {} → {} (alias={})", method, pattern, capturedStatus, registeredAlias);
        });

        // Reset builder state so the same mocker instance can be reused.
        this.urlPath     = null;
        this.statusCode  = 200;
        this.delayMs     = 0;
        this.alias       = null;
        this.extraHeaders.clear();

        return registeredAlias;
    }

    /**
     * Builds the glob pattern for {@code page.route()}.
     * Leading {@code **} matches any scheme/host; trailing {@code **} matches
     * any dynamic path segments or query parameters after the base path.
     */
    private String buildPattern() {
        return "**" + urlPath + "**";
    }

    private String toJson(Object obj) {
        if (obj == null)          return "{}";
        if (obj instanceof String) return (String) obj;
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[MOCK] Could not serialise response body for '{}': {}", urlPath, e.getMessage());
            return "{}";
        }
    }

    private String safePostBody(com.microsoft.playwright.Request request) {
        try { return request.postData(); } catch (Exception e) { return null; }
    }

    // ── Value type ──────────────────────────────────────────────────────────

    /**
     * Captures details of a request that was intercepted and fulfilled with mock data.
     *
     * @param url          the full request URL
     * @param method       HTTP method (GET, POST, …)
     * @param requestBody  POST body sent by the frontend (may be null for GET)
     * @param responseBody the JSON body that was returned as the mock response
     */
    public record InterceptedRequest(
            String url,
            String method,
            String requestBody,
            String responseBody
    ) {}
}
