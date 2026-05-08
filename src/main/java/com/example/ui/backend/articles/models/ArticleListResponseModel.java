package com.example.ui.backend.articles.models;

import java.util.List;

/**
 * Response wrapper for GET /api/articles
 *
 * <pre>{@code
 * {
 *   "articles": [ {...}, ... ],
 *   "articlesCount": 10
 * }
 * }</pre>
 */
public record ArticleListResponseModel(
        List<ArticleModel> articles,
        int articlesCount
) {}
