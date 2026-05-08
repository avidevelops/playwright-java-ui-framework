package com.example.ui.backend.articles.models;

/**
 * Response wrapper for GET /api/articles/{slug}
 *
 * <pre>{@code
 * {
 *   "article": { "slug": "...", "title": "...", ... }
 * }
 * }</pre>
 */
public record ArticleDetailResponseModel(ArticleModel article) {}
