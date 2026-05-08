package com.example.ui.backend.articles.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Matches the Conduit API article shape exactly.
 *
 * GET  /api/articles        → returns {@code { articles: [...], articlesCount: N }}
 * GET  /api/articles/{slug} → returns {@code { article: {...} }}
 *
 * Spec: https://realworld-docs.netlify.app/docs/specs/backend-specs/endpoints
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ArticleModel(
        String slug,
        String title,
        String description,
        String body,
        List<String> tagList,
        String createdAt,
        String updatedAt,
        boolean favorited,
        int favoritesCount,
        Author author
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Author(
            String username,
            String bio,
            String image,
            boolean following
    ) {}
}
